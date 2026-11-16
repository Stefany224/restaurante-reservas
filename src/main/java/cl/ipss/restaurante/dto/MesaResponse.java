package cl.ipss.restaurante.dto;

import cl.ipss.restaurante.enums.UbicacionMesa;

// MesaResponse son los datos de una mesa que se muestran en las vistas
public record MesaResponse(
        Long id,
        Integer numero,
        Integer capacidad,
        UbicacionMesa ubicacion,
        boolean activa
) {
}
