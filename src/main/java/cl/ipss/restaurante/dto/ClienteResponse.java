package cl.ipss.restaurante.dto;

import java.time.LocalDateTime;

// ClienteResponse son los datos del cliente que se muestran al personal junto a sus reservas activas
public record ClienteResponse(
        Long id,
        String nombre,
        String email,
        String telefono,
        LocalDateTime creadoEn,
        long reservasActivas
) {
}
