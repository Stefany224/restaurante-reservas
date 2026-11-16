package cl.ipss.restaurante.service;

import cl.ipss.restaurante.dto.MesaForm;
import cl.ipss.restaurante.dto.MesaResponse;

import java.util.List;

// MesaService define las operaciones de mesas que usa la administracion
public interface MesaService {

    // Todas las mesas, activas e inactivas
    List<MesaResponse> listar();

    // Una mesa por su id
    MesaResponse obtener(Long id);

    // Datos de la mesa para el formulario de editar
    MesaForm formularioEdicion(Long id);

    // Crear, actualizar y eliminar una mesa
    MesaResponse crear(MesaForm form);

    MesaResponse actualizar(Long id, MesaForm form);

    void eliminar(Long id);
}
