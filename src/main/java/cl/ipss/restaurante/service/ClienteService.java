package cl.ipss.restaurante.service;

import cl.ipss.restaurante.dto.ClienteResponse;

import java.util.List;

// ClienteService define la consulta de clientes, se crean solos en su primera reserva
public interface ClienteService {

    List<ClienteResponse> listar();
}
