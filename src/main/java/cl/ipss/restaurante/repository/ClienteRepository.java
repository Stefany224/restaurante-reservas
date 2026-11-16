package cl.ipss.restaurante.repository;

import cl.ipss.restaurante.entity.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

// ClienteRepository accede a la tabla cliente
@Repository
public interface ClienteRepository extends JpaRepository<Cliente, Long> {

    // Busca al cliente por su correo para reutilizarlo si vuelve a reservar
    Optional<Cliente> findByEmail(String email);

    // Todos los clientes ordenados por nombre
    List<Cliente> findAllByOrderByNombreAsc();
}
