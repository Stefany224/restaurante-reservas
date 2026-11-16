package cl.ipss.restaurante.enums;

// Filtros del panel de reservas, finalizada no se guarda en la base y se calcula con las activas cuya hora ya paso
public enum FiltroReserva {
    ACTIVAS("Activas"),
    CANCELADAS("Canceladas"),
    FINALIZADAS("Finalizadas");

    // Texto que aparece en el selector de filtros
    private final String etiqueta;

    // Constructor y getter de la etiqueta
    FiltroReserva(String etiqueta) {
        this.etiqueta = etiqueta;
    }

    public String getEtiqueta() {
        return etiqueta;
    }
}
