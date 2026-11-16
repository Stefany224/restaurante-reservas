package cl.ipss.restaurante.exception;

// Se lanza al crear o editar una mesa con un numero que ya usa otra
public class DatoDuplicadoException extends ReglaNegocioException {

    // Version de la clase para cuando Java la serializa
    private static final long serialVersionUID = 1L;

    // Arma el mensaje con el campo repetido y su valor
    public DatoDuplicadoException(String campo, Object valor) {
        super("Ya existe un registro con " + campo + " " + valor);
    }
}
