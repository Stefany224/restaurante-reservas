package cl.ipss.restaurante.exception;

// Se lanza cuando no existe la mesa o la reserva que se pidio y muestra la pagina 404
public class RecursoNoEncontradoException extends RuntimeException {

    // Version de la clase para cuando Java la serializa
    private static final long serialVersionUID = 1L;

    // Arma el mensaje con lo que se busco y su id
    public RecursoNoEncontradoException(String recurso, Long id) {
        super("No existe " + recurso + " con id " + id);
    }
}
