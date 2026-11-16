package cl.ipss.restaurante.exception;

// Se lanza cuando la mesa ya esta reservada en ese turno o esta desactivada
public class MesaNoDisponibleException extends ReglaNegocioException {

    // Version de la clase para cuando Java la serializa
    private static final long serialVersionUID = 1L;

    // Recibe el mensaje ya armado desde el service
    public MesaNoDisponibleException(String mensaje) {
        super(mensaje);
    }
}
