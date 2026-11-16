package cl.ipss.restaurante.exception;

// Se lanza cuando se buscan mesas para una cantidad de personas fuera del rango permitido
public class CantidadPersonasInvalidaException extends ReglaNegocioException {

    // Version de la clase para cuando Java la serializa
    private static final long serialVersionUID = 1L;

    // Arma el mensaje con el minimo y el maximo permitido
    public CantidadPersonasInvalidaException(int minimo, int maximo) {
        super("La cantidad de personas debe estar entre " + minimo + " y " + maximo);
    }
}
