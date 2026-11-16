package cl.ipss.restaurante.service.impl;

import cl.ipss.restaurante.dto.MesaForm;
import cl.ipss.restaurante.dto.MesaResponse;
import cl.ipss.restaurante.entity.Mesa;
import cl.ipss.restaurante.exception.DatoDuplicadoException;
import cl.ipss.restaurante.exception.MesaConReservasException;
import cl.ipss.restaurante.exception.RecursoNoEncontradoException;
import cl.ipss.restaurante.mapper.MesaMapper;
import cl.ipss.restaurante.repository.MesaRepository;
import cl.ipss.restaurante.repository.ReservaRepository;
import cl.ipss.restaurante.service.MesaService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

// MesaServiceImpl se encarga de manejar las mesas, crear, editar, eliminar y listarlas para el admin
@Service
@Transactional
public class MesaServiceImpl implements MesaService {

    // Logger para dejar registro de lo que pasa con las mesas en la consola y en el archivo de logs
    private static final Logger log = LoggerFactory.getLogger(MesaServiceImpl.class);

    // Repositorios y mapper que usa la clase para trabajar con la base de datos
    private final MesaRepository mesaRepository;
    private final ReservaRepository reservaRepository;
    private final MesaMapper mapper;

    // Spring nos entrega los repositorios y el mapper por el constructor
    public MesaServiceImpl(MesaRepository mesaRepository, ReservaRepository reservaRepository, MesaMapper mapper) {
        this.mesaRepository = mesaRepository;
        this.reservaRepository = reservaRepository;
        this.mapper = mapper;
    }

    // Listamos todas las mesas ordenadas por numero
    @Override
    @Transactional(readOnly = true)
    public List<MesaResponse> listar() {
        log.info("--- command receive to list tables ---");
        return mesaRepository.findAllByOrderByNumeroAsc().stream().map(mapper::aRespuesta).toList();
    }

    // Buscamos una mesa por su id
    @Override
    @Transactional(readOnly = true)
    public MesaResponse obtener(Long id) {
        return mapper.aRespuesta(buscar(id));
    }

    // Traemos los datos de la mesa para llenar el formulario de editar
    @Override
    @Transactional(readOnly = true)
    public MesaForm formularioEdicion(Long id) {
        return mapper.aFormulario(buscar(id));
    }

    // Creamos una mesa nueva siempre que su numero no este ocupado
    @Override
    public MesaResponse crear(MesaForm form) {
        log.info("--- command receive to create table: {} ---", form.getNumero());
        if (mesaRepository.existsByNumero(form.getNumero())) {
            log.warn("--- table number already exists: {} ---", form.getNumero());
            throw new DatoDuplicadoException("el número de mesa", form.getNumero());
        }
        Mesa guardada = mesaRepository.save(mapper.aEntidad(form));
        log.info("--- finish create table: {} - id {} ---", guardada.getNumero(), guardada.getId());
        return mapper.aRespuesta(guardada);
    }

    // Actualizamos una mesa que ya existe
    @Override
    public MesaResponse actualizar(Long id, MesaForm form) {
        log.info("--- command receive to update table: {} ---", id);
        Mesa mesa = buscar(id);
        // se descarta la misma mesa para que pueda guardarse sin cambiar su numero
        if (mesaRepository.existsByNumeroAndIdNot(form.getNumero(), id)) {
            log.warn("--- table number already exists: {} ---", form.getNumero());
            throw new DatoDuplicadoException("el número de mesa", form.getNumero());
        }
        mapper.actualizarEntidad(mesa, form);
        Mesa guardada = mesaRepository.save(mesa);
        log.info("--- finish update table: {} ---", id);
        return mapper.aRespuesta(guardada);
    }

    // Eliminamos una mesa solo si nunca ha tenido reservas, si las tiene se debe desactivar
    @Override
    public void eliminar(Long id) {
        log.info("--- command receive to delete table: {} ---", id);
        Mesa mesa = buscar(id);
        if (reservaRepository.existsByMesaId(id)) {
            log.warn("--- table has reservations, cannot delete: {} ---", id);
            throw new MesaConReservasException(mesa.getNumero());
        }
        mesaRepository.delete(mesa);
        log.info("--- finish delete table: {} ---", id);
    }

    // funcion para buscar una mesa por su id y si no existe lanza el error 404
    private Mesa buscar(Long id) {
        return mesaRepository.findById(id).orElseThrow(() -> {
            log.warn("--- table not found: {} ---", id);
            return new RecursoNoEncontradoException("la mesa", id);
        });
    }
}
