package cl.ipss.restaurante.exception;

// Se lanza al eliminar una mesa que tiene reservas, porque esas reservas quedarian sin mesa
public class MesaConReservasException extends ReglaNegocioException {

    // Version de la clase para cuando Java la serializa
    private static final long serialVersionUID = 1L;

    // Arma el mensaje sugiriendo desactivar la mesa
    public MesaConReservasException(Integer numeroMesa) {
        super("La mesa " + numeroMesa + " tiene reservas registradas; desactívala en lugar de eliminarla");
    }
}
