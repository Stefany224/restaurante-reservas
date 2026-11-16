package cl.ipss.restaurante.service.impl;

import cl.ipss.restaurante.dto.MesaResponse;
import cl.ipss.restaurante.dto.ReservaForm;
import cl.ipss.restaurante.dto.ReservaResponse;
import cl.ipss.restaurante.entity.Cliente;
import cl.ipss.restaurante.entity.Mesa;
import cl.ipss.restaurante.entity.Reserva;
import cl.ipss.restaurante.enums.EstadoReserva;
import cl.ipss.restaurante.enums.FiltroReserva;
import cl.ipss.restaurante.exception.CantidadPersonasInvalidaException;
import cl.ipss.restaurante.exception.CapacidadExcedidaException;
import cl.ipss.restaurante.exception.FechaHoraInvalidaException;
import cl.ipss.restaurante.exception.MesaNoDisponibleException;
import cl.ipss.restaurante.exception.RecursoNoEncontradoException;
import cl.ipss.restaurante.exception.ReservaNoModificableException;
import cl.ipss.restaurante.mapper.ClienteMapper;
import cl.ipss.restaurante.mapper.MesaMapper;
import cl.ipss.restaurante.mapper.ReservaMapper;
import cl.ipss.restaurante.repository.ClienteRepository;
import cl.ipss.restaurante.repository.MesaRepository;
import cl.ipss.restaurante.repository.ReservaRepository;
import cl.ipss.restaurante.service.ReservaService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

// ReservaServiceImpl tiene todas las reglas de negocio de las reservas, el controller solo llama a estos metodos
@Service
@Transactional
public class ReservaServiceImpl implements ReservaService {

    // Logger para dejar registro de cada reserva en la consola y en el archivo de logs
    private static final Logger log = LoggerFactory.getLogger(ReservaServiceImpl.class);

    // Turnos de almuerzo y cena en que el restaurante recibe reservas
    static final List<LocalTime> HORARIOS = List.of(
            LocalTime.of(13, 0), LocalTime.of(14, 0), LocalTime.of(15, 0),
            LocalTime.of(20, 0), LocalTime.of(21, 0), LocalTime.of(22, 0));

    // Maximo de dias de anticipacion para reservar
    static final int DIAS_MAXIMOS_ANTICIPACION = 60;

    // Rango de personas que se aceptan en una reserva
    static final int PERSONAS_MINIMAS = 1;
    static final int PERSONAS_MAXIMAS = 20;

    // Formato de fecha para los mensajes de error
    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("dd-MM-yyyy");

    // Repositorios y mappers que usa la clase
    private final ReservaRepository reservaRepository;
    private final MesaRepository mesaRepository;
    private final ClienteRepository clienteRepository;
    private final ReservaMapper reservaMapper;
    private final MesaMapper mesaMapper;
    private final ClienteMapper clienteMapper;

    // Spring nos entrega los repositorios y mappers por el constructor
    public ReservaServiceImpl(ReservaRepository reservaRepository, MesaRepository mesaRepository,
                              ClienteRepository clienteRepository, ReservaMapper reservaMapper,
                              MesaMapper mesaMapper, ClienteMapper clienteMapper) {
        this.reservaRepository = reservaRepository;
        this.mesaRepository = mesaRepository;
        this.clienteRepository = clienteRepository;
        this.reservaMapper = reservaMapper;
        this.mesaMapper = mesaMapper;
        this.clienteMapper = clienteMapper;
    }

    // Devuelve los turnos para los selectores de hora
    @Override
    public List<LocalTime> horarios() {
        return HORARIOS;
    }

    // Buscamos las mesas libres despues de validar las personas, la fecha y la hora
    @Override
    @Transactional(readOnly = true)
    public List<MesaResponse> mesasDisponibles(LocalDate fecha, LocalTime hora, int personas, Long reservaIdExcluida) {
        log.info("--- command receive to search tables: {} {} for {} ---", fecha, hora, personas);
        if (personas < PERSONAS_MINIMAS || personas > PERSONAS_MAXIMAS) {
            throw new CantidadPersonasInvalidaException(PERSONAS_MINIMAS, PERSONAS_MAXIMAS);
        }
        validarFechaHora(fecha, hora);
        List<MesaResponse> mesas = mesaRepository
                .buscarDisponibles(fecha, hora, personas, EstadoReserva.ACTIVA,
                        reservaIdExcluida == null ? 0L : reservaIdExcluida).stream()
                .map(mesaMapper::aRespuesta)
                .toList();
        log.info("--- finish search tables: {} available ---", mesas.size());
        return mesas;
    }

    // Creamos la reserva revisando el turno, la mesa, su capacidad y que no este ocupada
    @Override
    public ReservaResponse crear(ReservaForm form) {
        log.info("--- command receive to create reservation: table {} on {} {} ---",
                form.getMesaId(), form.getFecha(), form.getHora());
        validarFechaHora(form.getFecha(), form.getHora());
        Mesa mesa = mesaParaReservar(form.getMesaId(), form.getPersonas());
        // la mesa no puede tener otra reserva activa en el mismo turno
        if (reservaRepository.existsByMesaIdAndFechaAndHoraAndEstado(mesa.getId(), form.getFecha(), form.getHora(),
                EstadoReserva.ACTIVA)) {
            log.warn("--- table {} already booked on {} {} ---", mesa.getNumero(), form.getFecha(), form.getHora());
            throw mesaOcupada(mesa, form.getFecha(), form.getHora());
        }

        Reserva reserva = new Reserva();
        reserva.setMesa(mesa);
        reserva.setCliente(obtenerOCrearCliente(form));
        reservaMapper.actualizarEntidad(reserva, form);
        // toda reserva nace activa
        reserva.setEstado(EstadoReserva.ACTIVA);

        Reserva guardada = reservaRepository.save(reserva);
        log.info("--- finish create reservation: id {} ---", guardada.getId());
        return reservaMapper.aRespuesta(guardada);
    }

    // Reservas de un cliente de la mas reciente a la mas antigua
    @Override
    @Transactional(readOnly = true)
    public List<ReservaResponse> listarPorCliente(String email) {
        String correo = ClienteMapper.normalizarEmail(email);
        log.info("--- command receive to list reservations of customer: {} ---", correo);
        return reservaRepository.findByClienteEmailOrderByFechaDescHoraDesc(correo).stream()
                .map(reservaMapper::aRespuesta)
                .toList();
    }

    // Traemos los datos de la reserva para el formulario de modificar, solo si es del cliente y se puede modificar
    @Override
    @Transactional(readOnly = true)
    public ReservaForm formularioEdicion(Long id, String email) {
        Reserva reserva = buscarDelCliente(id, email);
        verificarModificable(reserva);
        return reservaMapper.aFormulario(reserva);
    }

    // Actualizamos la reserva volviendo a revisar todas las reglas
    @Override
    public ReservaResponse actualizar(Long id, String email, ReservaForm form) {
        log.info("--- command receive to update reservation: {} ---", id);
        Reserva reserva = buscarDelCliente(id, email);
        verificarModificable(reserva);
        validarFechaHora(form.getFecha(), form.getHora());
        Mesa mesa = mesaParaReservar(form.getMesaId(), form.getPersonas());
        // no se cuenta la misma reserva, asi puede quedarse en su mesa y turno y cambiar solo las personas
        if (reservaRepository.existsByMesaIdAndFechaAndHoraAndEstadoAndIdNot(mesa.getId(), form.getFecha(),
                form.getHora(), EstadoReserva.ACTIVA, id)) {
            log.warn("--- table {} already booked on {} {} ---", mesa.getNumero(), form.getFecha(), form.getHora());
            throw mesaOcupada(mesa, form.getFecha(), form.getHora());
        }

        reserva.setMesa(mesa);
        reservaMapper.actualizarEntidad(reserva, form);
        Reserva guardada = reservaRepository.save(reserva);
        log.info("--- finish update reservation: {} ---", id);
        return reservaMapper.aRespuesta(guardada);
    }

    // El cliente cancela una reserva suya
    @Override
    public ReservaResponse cancelar(Long id, String email) {
        log.info("--- command receive to cancel reservation: {} ---", id);
        return anular(buscarDelCliente(id, email));
    }

    // Listado de la administracion, segun los filtros se usa la consulta que corresponde para que filtre la base y no Java
    @Override
    @Transactional(readOnly = true)
    public List<ReservaResponse> listar(LocalDate fecha, FiltroReserva filtro) {
        log.info("--- command receive to list reservations: date {} filter {} ---",
                fecha == null ? "all" : fecha, filtro == null ? "all" : filtro);
        // separamos el momento actual en dia y hora para compararlo con las columnas de la tabla
        LocalDate hoy = LocalDate.now();
        LocalTime ahora = LocalTime.now();
        EstadoReserva activa = EstadoReserva.ACTIVA;

        List<Reserva> reservas;
        if (filtro == null) {
            reservas = (fecha == null)
                    ? reservaRepository.findAllByOrderByFechaDescHoraDesc()
                    : reservaRepository.findByFechaOrderByHoraAscIdAsc(fecha);
        } else {
            reservas = switch (filtro) {
                case ACTIVAS -> (fecha == null)
                        ? reservaRepository.buscarProximas(activa, hoy, ahora)
                        : reservaRepository.buscarProximasDelDia(activa, fecha, hoy, ahora);
                case FINALIZADAS -> (fecha == null)
                        ? reservaRepository.buscarFinalizadas(activa, hoy, ahora)
                        : reservaRepository.buscarFinalizadasDelDia(activa, fecha, hoy, ahora);
                case CANCELADAS -> (fecha == null)
                        ? reservaRepository.findByEstadoOrderByFechaDescHoraDesc(EstadoReserva.CANCELADA)
                        : reservaRepository.findByFechaAndEstadoOrderByHoraAscIdAsc(fecha, EstadoReserva.CANCELADA);
            };
        }
        return reservas.stream().map(reservaMapper::aRespuesta).toList();
    }

    // El personal cancela cualquier reserva
    @Override
    public ReservaResponse cancelarComoAdmin(Long id) {
        log.info("--- command receive to cancel reservation (admin): {} ---", id);
        Reserva reserva = reservaRepository.findConDetalleById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("la reserva", id));
        return anular(reserva);
    }

    // Pasa la reserva a CANCELADA sin borrarla, asi queda en el historial y la mesa se libera
    private ReservaResponse anular(Reserva reserva) {
        verificarModificable(reserva);
        reserva.setEstado(EstadoReserva.CANCELADA);
        Reserva guardada = reservaRepository.save(reserva);
        log.info("--- finish cancel reservation: {} ---", reserva.getId());
        return reservaMapper.aRespuesta(guardada);
    }

    // La hora debe ser un turno valido y la fecha no puede estar en el pasado ni muy lejos
    private void validarFechaHora(LocalDate fecha, LocalTime hora) {
        if (!HORARIOS.contains(hora)) {
            String turnos = HORARIOS.stream().map(LocalTime::toString).collect(Collectors.joining(", "));
            throw new FechaHoraInvalidaException("El restaurante recibe reservas solo a las " + turnos);
        }
        if (LocalDateTime.of(fecha, hora).isBefore(LocalDateTime.now())) {
            throw new FechaHoraInvalidaException("No se puede reservar en una fecha u hora que ya pasó");
        }
        if (fecha.isAfter(LocalDate.now().plusDays(DIAS_MAXIMOS_ANTICIPACION))) {
            throw new FechaHoraInvalidaException("Se puede reservar con un máximo de "
                    + DIAS_MAXIMOS_ANTICIPACION + " días de anticipación");
        }
    }

    // Busca la mesa bloqueandola y revisa que este activa y que alcance para las personas
    private Mesa mesaParaReservar(Long mesaId, Integer personas) {
        Mesa mesa = mesaRepository.buscarParaActualizar(mesaId).orElseThrow(() -> {
            log.warn("--- table not found: {} ---", mesaId);
            return new RecursoNoEncontradoException("la mesa", mesaId);
        });
        if (!mesa.isActiva()) {
            log.warn("--- table {} is inactive ---", mesa.getNumero());
            throw new MesaNoDisponibleException("La mesa " + mesa.getNumero() + " no está habilitada para reservas");
        }
        if (personas > mesa.getCapacidad()) {
            log.warn("--- table {} capacity {} exceeded: {} ---", mesa.getNumero(), mesa.getCapacidad(), personas);
            throw new CapacidadExcedidaException(mesa.getNumero(), mesa.getCapacidad(), personas);
        }
        return mesa;
    }

    // No se toca una reserva cancelada ni una cuya hora ya paso
    private void verificarModificable(Reserva reserva) {
        if (reserva.getEstado() == EstadoReserva.CANCELADA) {
            throw new ReservaNoModificableException(reserva.getId(), "ya está cancelada");
        }
        if (reserva.yaPaso()) {
            throw new ReservaNoModificableException(reserva.getId(), "su fecha y hora ya pasaron");
        }
    }

    // Busca la reserva y comprueba que sea del cliente, si es de otra persona responde que no existe para no mostrar reservas ajenas
    private Reserva buscarDelCliente(Long id, String email) {
        String correo = ClienteMapper.normalizarEmail(email);
        return reservaRepository.findConDetalleById(id)
                .filter(r -> r.getCliente().getEmail().equals(correo))
                .orElseThrow(() -> {
                    log.warn("--- reservation {} not found for customer {} ---", id, correo);
                    return new RecursoNoEncontradoException("la reserva", id);
                });
    }

    // Si el correo ya existe se reutiliza el cliente y se actualizan sus datos, si no se crea uno nuevo
    private Cliente obtenerOCrearCliente(ReservaForm form) {
        String correo = ClienteMapper.normalizarEmail(form.getEmail());
        return clienteRepository.findByEmail(correo)
                .map(existente -> {
                    clienteMapper.actualizarDatos(existente, form.getNombre(), form.getTelefono());
                    return existente;
                })
                .orElseGet(() -> clienteRepository.save(
                        clienteMapper.aEntidad(form.getNombre(), correo, form.getTelefono())));
    }

    // Arma el error de mesa ocupada con el dia y la hora
    private MesaNoDisponibleException mesaOcupada(Mesa mesa, LocalDate fecha, LocalTime hora) {
        return new MesaNoDisponibleException("La mesa " + mesa.getNumero() + " ya está reservada el "
                + fecha.format(FORMATO_FECHA) + " a las " + hora + "; elige otra mesa u horario");
    }
}
