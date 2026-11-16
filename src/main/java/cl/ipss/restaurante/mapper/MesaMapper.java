package cl.ipss.restaurante.mapper;

import cl.ipss.restaurante.dto.MesaForm;
import cl.ipss.restaurante.dto.MesaResponse;
import cl.ipss.restaurante.entity.Mesa;
import org.springframework.stereotype.Component;

// MesaMapper convierte entre los DTO de mesas y la entidad Mesa
@Component
public class MesaMapper {

    // Crea una mesa nueva con los datos del formulario
    public Mesa aEntidad(MesaForm f) {
        Mesa entidad = new Mesa();
        actualizarEntidad(entidad, f);
        return entidad;
    }

    // Copia los datos del formulario sobre una mesa que ya existe
    public void actualizarEntidad(Mesa entidad, MesaForm f) {
        entidad.setNumero(f.getNumero());
        entidad.setCapacidad(f.getCapacidad());
        entidad.setUbicacion(f.getUbicacion());
        entidad.setActiva(f.isActiva());
    }

    // Convierte la mesa en el DTO que se muestra
    public MesaResponse aRespuesta(Mesa m) {
        return new MesaResponse(m.getId(), m.getNumero(), m.getCapacidad(), m.getUbicacion(), m.isActiva());
    }

    // Llena el formulario de editar con los datos actuales
    public MesaForm aFormulario(Mesa m) {
        MesaForm f = new MesaForm();
        f.setNumero(m.getNumero());
        f.setCapacidad(m.getCapacidad());
        f.setUbicacion(m.getUbicacion());
        f.setActiva(m.isActiva());
        return f;
    }
}
