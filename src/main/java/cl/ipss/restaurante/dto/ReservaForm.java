package cl.ipss.restaurante.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.time.LocalTime;

// ReservaForm recibe los datos del formulario de reserva, aqui solo se valida el formato y las reglas las revisa el service
public class ReservaForm {

    // Mesa elegida en el buscador
    @NotNull(message = "Selecciona una mesa")
    private Long mesaId;

    // El input de fecha manda el formato yyyy-MM-dd
    @NotNull(message = "La fecha es obligatoria")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate fecha;

    // El select de horarios manda la hora como HH:mm
    @NotNull(message = "La hora es obligatoria")
    @DateTimeFormat(pattern = "HH:mm")
    private LocalTime hora;

    // Cantidad de personas, de 1 a 20
    @NotNull(message = "Indica cuántas personas asistirán")
    @Min(value = 1, message = "Debe asistir al menos 1 persona")
    @Max(value = 20, message = "Para más de 20 personas contacta directamente al restaurante")
    private Integer personas;

    // Nombre solo con letras y un espacio entre palabras, sin repetir la misma letra 3 veces seguidas
    @NotBlank(message = "El nombre es obligatorio")
    @Size(min = 2, max = 30, message = "El nombre debe tener entre 2 y 30 caracteres")
    @Pattern(regexp = "^(?!.*(\\p{L})\\1{2})\\p{L}+(?: \\p{L}+)*$",
            message = "El nombre solo puede tener letras separadas por un espacio, sin repetir una letra 3 veces seguidas")
    private String nombre;

    // Correo con forma real, el usuario y el dominio deben tener letras y no se aceptan caracteres repetidos 3 veces seguidas
    @NotBlank(message = "El correo es obligatorio")
    @Email(message = "El correo no tiene un formato válido")
    @Pattern(regexp = "^(?!.*([A-Za-z0-9])\\1{2})(?=[^@]*[A-Za-z])(?=[^@]*@[^.]*[A-Za-z])[A-Za-z0-9]+(?:[._+-][A-Za-z0-9]+)*"
            + "@[A-Za-z0-9]+(?:-[A-Za-z0-9]+)*(?:\\.[A-Za-z0-9]+(?:-[A-Za-z0-9]+)*)*\\.[A-Za-z]{2,}$",
            message = "Ingresa un correo válido, por ejemplo nombre@correo.cl")
    @Size(max = 120, message = "El correo no puede superar los 120 caracteres")
    private String email;

    // Telefono de 8 o 9 digitos que siempre queda con el formato X XXXX XXXX
    @NotBlank(message = "El teléfono es obligatorio")
    @Pattern(regexp = "^\\d \\d{4} \\d{4}$", message = "El teléfono debe tener 8 o 9 dígitos, por ejemplo 9 1234 5678")
    private String telefono;

    // Comentario opcional, si se escribe debe tener palabras de verdad y no simbolos o caracteres repetidos
    @Size(max = 200, message = "El comentario no puede superar los 200 caracteres")
    @Pattern(regexp = "(?s)^(?:\\s*|(?!.*(.)\\1{3})(?=(?:.*\\p{L}){3})[\\p{L}\\d\\s.,;:¡!¿?()'\"-]+)$",
            message = "El comentario debe ser un texto con palabras (letras, números y puntuación común), sin caracteres repetidos")
    private String comentario;

    // Getters y setters del formulario
    public Long getMesaId() {
        return mesaId;
    }

    public void setMesaId(Long mesaId) {
        this.mesaId = mesaId;
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public void setFecha(LocalDate fecha) {
        this.fecha = fecha;
    }

    public LocalTime getHora() {
        return hora;
    }

    public void setHora(LocalTime hora) {
        this.hora = hora;
    }

    public Integer getPersonas() {
        return personas;
    }

    public void setPersonas(Integer personas) {
        this.personas = personas;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getTelefono() {
        return telefono;
    }

    // Al recibir el formulario el telefono se ordena antes de validarlo
    public void setTelefono(String telefono) {
        this.telefono = normalizarTelefono(telefono);
    }

    // Deja solo los numeros, si son 8 le agrega el 9 adelante y si son 9 les pone los espacios
    static String normalizarTelefono(String texto) {
        if (texto == null) {
            return null;
        }
        String digitos = texto.replaceAll("\\D", "");
        if (digitos.length() == 8) {
            digitos = "9" + digitos;
        }
        if (digitos.length() == 9) {
            return digitos.charAt(0) + " " + digitos.substring(1, 5) + " " + digitos.substring(5);
        }
        // si no son 8 o 9 digitos se deja como venia para que la validacion muestre el error
        return texto;
    }

    public String getComentario() {
        return comentario;
    }

    public void setComentario(String comentario) {
        this.comentario = comentario;
    }
}
