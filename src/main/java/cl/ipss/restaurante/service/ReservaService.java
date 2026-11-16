package cl.ipss.restaurante.service;

import cl.ipss.restaurante.dto.MesaResponse;
import cl.ipss.restaurante.dto.ReservaForm;
import cl.ipss.restaurante.dto.ReservaResponse;
import cl.ipss.restaurante.enums.FiltroReserva;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

// ReservaService define las operaciones de reservas, las del cliente reciben su correo para que solo vea y cambie las suyas
public interface ReservaService {

    // Turnos en que se reciben reservas
    List<LocalTime> horarios();

    // Mesas libres para un dia, hora y cantidad de personas
    List<MesaResponse> mesasDisponibles(LocalDate fecha, LocalTime hora, int personas, Long reservaIdExcluida);

    // Crea la reserva y al cliente si es su primera vez
    ReservaResponse crear(ReservaForm form);

    // Reservas de un cliente
    List<ReservaResponse> listarPorCliente(String email);

    // Modificar y cancelar una reserva propia
    ReservaForm formularioEdicion(Long id, String email);

    ReservaResponse actualizar(Long id, String email, ReservaForm form);

    ReservaResponse cancelar(Long id, String email);

    // Para la administracion, listar con filtros y cancelar cualquier reserva
    List<ReservaResponse> listar(LocalDate fecha, FiltroReserva filtro);

    ReservaResponse cancelarComoAdmin(Long id);
}
