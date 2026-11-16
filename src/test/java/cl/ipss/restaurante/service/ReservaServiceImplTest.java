package cl.ipss.restaurante.service;

import cl.ipss.restaurante.dto.ReservaForm;
import cl.ipss.restaurante.dto.ReservaResponse;
import cl.ipss.restaurante.entity.Cliente;
import cl.ipss.restaurante.entity.Mesa;
import cl.ipss.restaurante.entity.Reserva;
import cl.ipss.restaurante.enums.EstadoReserva;
import cl.ipss.restaurante.enums.UbicacionMesa;
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
import cl.ipss.restaurante.service.impl.ReservaServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

// Pruebas unitarias de ReservaServiceImpl, los repositorios se simulan con Mockito asi no hace falta levantar Spring ni la base
@ExtendWith(MockitoExtension.class)
class ReservaServiceImplTest {

    // Fecha y hora validas que usan casi todas las pruebas
    private static final LocalDate PASADO_MANANA = LocalDate.now().plusDays(2);
    private static final LocalTime OCHO_PM = LocalTime.of(20, 0);

    // Repositorios simulados
    @Mock
    private ReservaRepository reservaRepository;
    @Mock
    private MesaRepository mesaRepository;
    @Mock
    private ClienteRepository clienteRepository;

    // Service que se prueba
    private ReservaServiceImpl service;

    // Antes de cada prueba se arma el service con los repositorios simulados y los mappers reales
    @BeforeEach
    void preparar() {
        service = new ReservaServiceImpl(reservaRepository, mesaRepository, clienteRepository,
                new ReservaMapper(), new MesaMapper(), new ClienteMapper());
    }

    // Datos de prueba, el id se pone a mano porque normalmente lo asigna la base
    private Mesa mesa(Long id, int numero, int capacidad, boolean activa) {
        Mesa m = new Mesa();
        ReflectionTestUtils.setField(m, "id", id);
        m.setNumero(numero);
        m.setCapacidad(capacidad);
        m.setUbicacion(UbicacionMesa.SALON);
        m.setActiva(activa);
        return m;
    }

    // Formulario de prueba con espacios de mas y mayusculas en el correo para probar que se limpian
    private ReservaForm form(Long mesaId, int personas, LocalDate fecha, LocalTime hora) {
        ReservaForm f = new ReservaForm();
        f.setMesaId(mesaId);
        f.setPersonas(personas);
        f.setFecha(fecha);
        f.setHora(hora);
        f.setNombre("  Ana Pérez ");
        f.setEmail(" Ana.Perez@Correo.CL ");
        f.setTelefono("9 1234 5678");
        f.setComentario("   ");
        return f;
    }

    // Reserva de prueba de Ana para dentro de dos dias
    private Reserva reserva(EstadoReserva estado) {
        Cliente c = new Cliente();
        ReflectionTestUtils.setField(c, "id", 10L);
        c.setNombre("Ana Pérez");
        c.setEmail("ana.perez@correo.cl");
        c.setTelefono("9 1234 5678");
        Reserva r = new Reserva();
        ReflectionTestUtils.setField(r, "id", 5L);
        r.setMesa(mesa(1L, 3, 4, true));
        r.setCliente(c);
        r.setFecha(PASADO_MANANA);
        r.setHora(OCHO_PM);
        r.setPersonas(2);
        r.setEstado(estado);
        return r;
    }

    // Pruebas al crear una reserva
    @Test
    void creaReservaActivaYCreaElClienteConDatosLimpios() {
        when(mesaRepository.buscarParaActualizar(1L)).thenReturn(Optional.of(mesa(1L, 3, 4, true)));
        when(reservaRepository.existsByMesaIdAndFechaAndHoraAndEstado(1L, PASADO_MANANA, OCHO_PM, EstadoReserva.ACTIVA))
                .thenReturn(false);
        when(clienteRepository.findByEmail("ana.perez@correo.cl")).thenReturn(Optional.empty());
        when(clienteRepository.save(any(Cliente.class))).thenAnswer(inv -> inv.getArgument(0));
        when(reservaRepository.save(any(Reserva.class))).thenAnswer(inv -> {
            Reserva r = inv.getArgument(0);
            ReflectionTestUtils.setField(r, "id", 7L);
            return r;
        });

        ReservaResponse r = service.crear(form(1L, 4, PASADO_MANANA, OCHO_PM));

        assertEquals(7L, r.id());
        assertEquals(EstadoReserva.ACTIVA, r.estado());
        assertEquals("ana.perez@correo.cl", r.emailCliente());
        assertEquals("Ana Pérez", r.nombreCliente());
        assertEquals(3, r.numeroMesa());
        assertEquals(null, r.comentario());
        assertTrue(r.modificable());
    }

    @Test
    void rechazaMasPersonasQueLaCapacidadDeLaMesa() {
        when(mesaRepository.buscarParaActualizar(1L)).thenReturn(Optional.of(mesa(1L, 1, 2, true)));

        assertThrows(CapacidadExcedidaException.class, () -> service.crear(form(1L, 4, PASADO_MANANA, OCHO_PM)));
        verify(reservaRepository, never()).save(any());
    }

    @Test
    void rechazaMesaYaReservadaEnEseTurno() {
        when(mesaRepository.buscarParaActualizar(1L)).thenReturn(Optional.of(mesa(1L, 3, 4, true)));
        when(reservaRepository.existsByMesaIdAndFechaAndHoraAndEstado(1L, PASADO_MANANA, OCHO_PM, EstadoReserva.ACTIVA))
                .thenReturn(true);

        assertThrows(MesaNoDisponibleException.class, () -> service.crear(form(1L, 2, PASADO_MANANA, OCHO_PM)));
        verify(reservaRepository, never()).save(any());
    }

    @Test
    void rechazaMesaDesactivada() {
        when(mesaRepository.buscarParaActualizar(9L)).thenReturn(Optional.of(mesa(9L, 9, 2, false)));

        assertThrows(MesaNoDisponibleException.class, () -> service.crear(form(9L, 2, PASADO_MANANA, OCHO_PM)));
    }

    @Test
    void rechazaHoraFueraDelHorarioDeAtencion() {
        assertThrows(FechaHoraInvalidaException.class,
                () -> service.crear(form(1L, 2, PASADO_MANANA, LocalTime.of(17, 0))));
    }

    @Test
    void rechazaFechaPasada() {
        assertThrows(FechaHoraInvalidaException.class,
                () -> service.crear(form(1L, 2, LocalDate.now().minusDays(1), OCHO_PM)));
    }

    // Pruebas al modificar y cancelar
    @Test
    void modificaLaReservaSinChocarConsigoMisma() {
        when(reservaRepository.findConDetalleById(5L)).thenReturn(Optional.of(reserva(EstadoReserva.ACTIVA)));
        when(mesaRepository.buscarParaActualizar(1L)).thenReturn(Optional.of(mesa(1L, 3, 4, true)));
        when(reservaRepository.existsByMesaIdAndFechaAndHoraAndEstadoAndIdNot(1L, PASADO_MANANA, OCHO_PM,
                EstadoReserva.ACTIVA, 5L)).thenReturn(false);
        when(reservaRepository.save(any(Reserva.class))).thenAnswer(inv -> inv.getArgument(0));

        ReservaResponse r = service.actualizar(5L, "ana.perez@correo.cl", form(1L, 4, PASADO_MANANA, OCHO_PM));

        assertEquals(4, r.personas());
    }

    @Test
    void cancelaUnaReservaActiva() {
        when(reservaRepository.findConDetalleById(5L)).thenReturn(Optional.of(reserva(EstadoReserva.ACTIVA)));
        when(reservaRepository.save(any(Reserva.class))).thenAnswer(inv -> inv.getArgument(0));

        // el correo se compara sin importar mayusculas ni espacios
        ReservaResponse r = service.cancelar(5L, " ANA.PEREZ@correo.cl ");

        assertEquals(EstadoReserva.CANCELADA, r.estado());
    }

    @Test
    void noSePuedeCancelarDosVeces() {
        when(reservaRepository.findConDetalleById(5L)).thenReturn(Optional.of(reserva(EstadoReserva.CANCELADA)));

        assertThrows(ReservaNoModificableException.class, () -> service.cancelar(5L, "ana.perez@correo.cl"));
        verify(reservaRepository, never()).save(any());
    }

    @Test
    void unClienteNoPuedeTocarLaReservaDeOtro() {
        when(reservaRepository.findConDetalleById(5L)).thenReturn(Optional.of(reserva(EstadoReserva.ACTIVA)));

        assertThrows(RecursoNoEncontradoException.class, () -> service.cancelar(5L, "otra.persona@correo.cl"));
    }
}
