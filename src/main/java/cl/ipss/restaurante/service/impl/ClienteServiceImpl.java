package cl.ipss.restaurante.service.impl;

import cl.ipss.restaurante.dto.ClienteResponse;
import cl.ipss.restaurante.enums.EstadoReserva;
import cl.ipss.restaurante.mapper.ClienteMapper;
import cl.ipss.restaurante.repository.ClienteRepository;
import cl.ipss.restaurante.repository.ConteoReservasPorCliente;
import cl.ipss.restaurante.repository.ReservaRepository;
import cl.ipss.restaurante.service.ClienteService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

// ClienteServiceImpl arma el listado de clientes para el personal
@Service
@Transactional(readOnly = true)
public class ClienteServiceImpl implements ClienteService {

    // Logger para dejar registro de las consultas
    private static final Logger log = LoggerFactory.getLogger(ClienteServiceImpl.class);

    // Repositorios y mapper que usa la clase
    private final ClienteRepository clienteRepository;
    private final ReservaRepository reservaRepository;
    private final ClienteMapper mapper;

    // Spring entrega los repositorios y el mapper por el constructor
    public ClienteServiceImpl(ClienteRepository clienteRepository, ReservaRepository reservaRepository,
                              ClienteMapper mapper) {
        this.clienteRepository = clienteRepository;
        this.reservaRepository = reservaRepository;
        this.mapper = mapper;
    }

    // Listamos los clientes con sus reservas activas usando solo dos consultas sin importar cuantos clientes haya
    @Override
    public List<ClienteResponse> listar() {
        log.info("--- command receive to list customers ---");
        // primero traemos el total de reservas activas de cada cliente y lo guardamos por su id
        Map<Long, Long> activasPorCliente = reservaRepository.contarPorCliente(EstadoReserva.ACTIVA).stream()
                .collect(Collectors.toMap(ConteoReservasPorCliente::getClienteId, ConteoReservasPorCliente::getTotal));
        // despues cruzamos ese total con cada cliente y si no tiene reservas queda en 0
        return clienteRepository.findAllByOrderByNombreAsc().stream()
                .map(c -> mapper.aRespuesta(c, activasPorCliente.getOrDefault(c.getId(), 0L)))
                .toList();
    }
}
