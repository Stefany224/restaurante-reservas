package cl.ipss.restaurante.exception;

// Se lanza cuando la reserva es para mas personas de las que caben en la mesa
public class CapacidadExcedidaException extends ReglaNegocioException {

    // Version de la clase para cuando Java la serializa
    private static final long serialVersionUID = 1L;

    // Arma el mensaje con la mesa, su capacidad y las personas pedidas
    public CapacidadExcedidaException(Integer numeroMesa, Integer capacidad, Integer personas) {
        super("La mesa " + numeroMesa + " es para " + capacidad + " personas y la reserva es para " + personas
                + "; elige una mesa más grande");
    }
}
