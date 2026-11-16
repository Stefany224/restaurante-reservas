package cl.ipss.restaurante.mapper;

import cl.ipss.restaurante.dto.ClienteResponse;
import cl.ipss.restaurante.entity.Cliente;
import org.springframework.stereotype.Component;

// ClienteMapper convierte entre los datos del cliente y la entidad Cliente
@Component
public class ClienteMapper {

    // Crea un cliente nuevo con los datos limpios
    public Cliente aEntidad(String nombre, String email, String telefono) {
        Cliente c = new Cliente();
        actualizarDatos(c, nombre, telefono);
        c.setEmail(normalizarEmail(email));
        return c;
    }

    // Actualiza nombre y telefono por si cambiaron desde su ultima reserva
    public void actualizarDatos(Cliente c, String nombre, String telefono) {
        c.setNombre(nombre.trim());
        c.setTelefono(telefono.trim());
    }

    // Convierte el cliente en el DTO que ve el personal
    public ClienteResponse aRespuesta(Cliente c, long reservasActivas) {
        return new ClienteResponse(c.getId(), c.getNombre(), c.getEmail(), c.getTelefono(), c.getCreadoEn(),
                reservasActivas);
    }

    // El correo se compara siempre en minusculas y sin espacios
    public static String normalizarEmail(String email) {
        return email == null ? "" : email.trim().toLowerCase();
    }
}
