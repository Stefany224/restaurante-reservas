package cl.ipss.restaurante.exception;

// Se lanza cuando la fecha ya paso, esta muy lejos o la hora no es un turno de atencion
public class FechaHoraInvalidaException extends ReglaNegocioException {

    // Version de la clase para cuando Java la serializa
    private static final long serialVersionUID = 1L;

    // Recibe el mensaje ya armado desde el service
    public FechaHoraInvalidaException(String mensaje) {
        super(mensaje);
    }
}
