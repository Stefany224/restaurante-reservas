package cl.ipss.restaurante.exception;

// Se lanza al intentar modificar o cancelar una reserva cancelada o cuya hora ya paso
public class ReservaNoModificableException extends ReglaNegocioException {

    // Version de la clase para cuando Java la serializa
    private static final long serialVersionUID = 1L;

    // Arma el mensaje con la reserva y el motivo
    public ReservaNoModificableException(Long id, String motivo) {
        super("La reserva " + id + " no se puede modificar: " + motivo);
    }
}
