package cl.ipss.restaurante.dto;

import cl.ipss.restaurante.enums.UbicacionMesa;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

// MesaForm recibe los datos del formulario de mesas, es clase y no record porque Thymeleaf necesita sus setters
public class MesaForm {

    // Numero visible de la mesa
    @NotNull(message = "El número de mesa es obligatorio")
    @Min(value = 1, message = "El número de mesa debe ser mayor o igual a 1")
    @Max(value = 999, message = "El número de mesa no puede superar 999")
    private Integer numero;

    // Cuantas personas caben
    @NotNull(message = "La capacidad es obligatoria")
    @Min(value = 1, message = "La mesa debe tener capacidad para al menos 1 persona")
    @Max(value = 20, message = "La capacidad no puede superar las 20 personas")
    private Integer capacidad;

    // Zona donde esta la mesa
    @NotNull(message = "La ubicación es obligatoria")
    private UbicacionMesa ubicacion;

    // Una mesa nueva queda activa por defecto
    private boolean activa = true;

    // Getters y setters del formulario
    public Integer getNumero() {
        return numero;
    }

    public void setNumero(Integer numero) {
        this.numero = numero;
    }

    public Integer getCapacidad() {
        return capacidad;
    }

    public void setCapacidad(Integer capacidad) {
        this.capacidad = capacidad;
    }

    public UbicacionMesa getUbicacion() {
        return ubicacion;
    }

    public void setUbicacion(UbicacionMesa ubicacion) {
        this.ubicacion = ubicacion;
    }

    public boolean isActiva() {
        return activa;
    }

    public void setActiva(boolean activa) {
        this.activa = activa;
    }
}
