package cl.ipss.restaurante.enums;

// Zonas del restaurante, cada una con el texto que se muestra en las vistas
public enum UbicacionMesa {
    SALON("Salón"),
    TERRAZA("Terraza"),
    PRIVADO("Sala privada");

    // Texto visible de cada zona
    private final String etiqueta;

    // Constructor y getter de la etiqueta
    UbicacionMesa(String etiqueta) {
        this.etiqueta = etiqueta;
    }

    public String getEtiqueta() {
        return etiqueta;
    }
}
