package cl.ipss.restaurante.mapper;

import cl.ipss.restaurante.dto.ReservaForm;
import cl.ipss.restaurante.dto.ReservaResponse;
import cl.ipss.restaurante.entity.Reserva;
import cl.ipss.restaurante.enums.EstadoReserva;
import org.springframework.stereotype.Component;

// ReservaMapper convierte entre los DTO de reservas y la entidad Reserva
@Component
public class ReservaMapper {

    // Convierte la reserva en el DTO que se muestra, se llama dentro de la transaccion porque lee la mesa y el cliente
    public ReservaResponse aRespuesta(Reserva r) {
        boolean modificable = r.getEstado() == EstadoReserva.ACTIVA && !r.yaPaso();
        return new ReservaResponse(r.getId(),
                r.getMesa().getNumero(), r.getMesa().getUbicacion(),
                r.getCliente().getNombre(), r.getCliente().getEmail(), r.getCliente().getTelefono(),
                r.getFecha(), r.getHora(), r.getPersonas(), r.getComentario(), r.getEstado(), modificable);
    }

    // Llena el formulario de modificar con los datos actuales de la reserva
    public ReservaForm aFormulario(Reserva r) {
        ReservaForm f = new ReservaForm();
        f.setMesaId(r.getMesa().getId());
        f.setFecha(r.getFecha());
        f.setHora(r.getHora());
        f.setPersonas(r.getPersonas());
        f.setComentario(r.getComentario());
        f.setNombre(r.getCliente().getNombre());
        f.setEmail(r.getCliente().getEmail());
        f.setTelefono(r.getCliente().getTelefono());
        return f;
    }

    // Copia fecha, hora, personas y comentario, la mesa la asigna el service
    public void actualizarEntidad(Reserva r, ReservaForm f) {
        r.setFecha(f.getFecha());
        r.setHora(f.getHora());
        r.setPersonas(f.getPersonas());
        r.setComentario(limpiar(f.getComentario()));
    }

    // Un comentario vacio se guarda como null
    public static String limpiar(String texto) {
        return (texto == null || texto.isBlank()) ? null : texto.trim();
    }
}
