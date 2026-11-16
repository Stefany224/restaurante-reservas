package cl.ipss.restaurante.repository;

// Proyeccion de Spring Data, recibe solo el id del cliente y su total de reservas en vez de entidades completas
public interface ConteoReservasPorCliente {

    Long getClienteId();

    Long getTotal();
}
