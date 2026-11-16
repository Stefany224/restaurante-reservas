package cl.ipss.restaurante.exception;

// ReglaNegocioException es la base de todas las reglas que se pueden romper, asi los controllers atrapan una sola clase
public abstract class ReglaNegocioException extends RuntimeException {

    // Version de la clase para cuando Java la serializa
    private static final long serialVersionUID = 1L;

    // El mensaje es lo que ve el usuario en la pagina
    protected ReglaNegocioException(String mensaje) {
        super(mensaje);
    }
}
