package cl.ipss.restaurante.dto;

import cl.ipss.restaurante.enums.EstadoReserva;
import cl.ipss.restaurante.enums.UbicacionMesa;

import java.time.LocalDate;
import java.time.LocalTime;

// ReservaResponse es la reserva lista para mostrar, con los datos de la mesa y del cliente ya incluidos y si todavia se puede modificar
public record ReservaResponse(
        Long id,
        Integer numeroMesa,
        UbicacionMesa ubicacionMesa,
        String nombreCliente,
        String emailCliente,
        String telefonoCliente,
        LocalDate fecha,
        LocalTime hora,
        Integer personas,
        String comentario,
        EstadoReserva estado,
        boolean modificable
) {
}
