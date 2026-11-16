package cl.ipss.restaurante.repository;

import cl.ipss.restaurante.entity.Reserva;
import cl.ipss.restaurante.enums.EstadoReserva;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

// ReservaRepository accede a la tabla reserva, la mayoria de las consultas salen del nombre del metodo
@Repository
public interface ReservaRepository extends JpaRepository<Reserva, Long> {

    // Revisan si la mesa ya esta tomada en ese turno, la segunda no cuenta la reserva que se esta modificando
    boolean existsByMesaIdAndFechaAndHoraAndEstado(Long mesaId, LocalDate fecha, LocalTime hora, EstadoReserva estado);

    boolean existsByMesaIdAndFechaAndHoraAndEstadoAndIdNot(Long mesaId, LocalDate fecha, LocalTime hora,
                                                          EstadoReserva estado, Long id);

    // Si la mesa aparece en alguna reserva no se puede eliminar
    boolean existsByMesaId(Long mesaId);

    // Cuenta en una sola consulta cuantas reservas tiene cada cliente, asi no se hace una consulta por cliente
    @Query("""
            select r.cliente.id as clienteId, count(r) as total
            from Reserva r
            where r.estado = :estado
            group by r.cliente.id
            """)
    List<ConteoReservasPorCliente> contarPorCliente(@Param("estado") EstadoReserva estado);

    // Listados de reservas, EntityGraph trae la mesa y el cliente en la misma consulta
    @EntityGraph(attributePaths = {"mesa", "cliente"})
    List<Reserva> findByClienteEmailOrderByFechaDescHoraDesc(String email);

    @EntityGraph(attributePaths = {"mesa", "cliente"})
    List<Reserva> findAllByOrderByFechaDescHoraDesc();

    @EntityGraph(attributePaths = {"mesa", "cliente"})
    List<Reserva> findByFechaOrderByHoraAscIdAsc(LocalDate fecha);

    @EntityGraph(attributePaths = {"mesa", "cliente"})
    List<Reserva> findByEstadoOrderByFechaDescHoraDesc(EstadoReserva estado);

    @EntityGraph(attributePaths = {"mesa", "cliente"})
    List<Reserva> findByFechaAndEstadoOrderByHoraAscIdAsc(LocalDate fecha, EstadoReserva estado);

    // Activas que todavia no llegan, con todas las fechas o solo las de un dia
    @EntityGraph(attributePaths = {"mesa", "cliente"})
    @Query("""
            select r from Reserva r
            where r.estado = :estado
              and (r.fecha > :hoy or (r.fecha = :hoy and r.hora >= :ahora))
            order by r.fecha, r.hora, r.id
            """)
    List<Reserva> buscarProximas(@Param("estado") EstadoReserva estado,
                                 @Param("hoy") LocalDate hoy, @Param("ahora") LocalTime ahora);

    @EntityGraph(attributePaths = {"mesa", "cliente"})
    @Query("""
            select r from Reserva r
            where r.estado = :estado and r.fecha = :fecha
              and (r.fecha > :hoy or (r.fecha = :hoy and r.hora >= :ahora))
            order by r.hora, r.id
            """)
    List<Reserva> buscarProximasDelDia(@Param("estado") EstadoReserva estado, @Param("fecha") LocalDate fecha,
                                       @Param("hoy") LocalDate hoy, @Param("ahora") LocalTime ahora);

    // Finalizadas, siguen activas porque nadie las cancelo pero su hora ya paso
    @EntityGraph(attributePaths = {"mesa", "cliente"})
    @Query("""
            select r from Reserva r
            where r.estado = :estado
              and (r.fecha < :hoy or (r.fecha = :hoy and r.hora < :ahora))
            order by r.fecha desc, r.hora desc, r.id
            """)
    List<Reserva> buscarFinalizadas(@Param("estado") EstadoReserva estado,
                                    @Param("hoy") LocalDate hoy, @Param("ahora") LocalTime ahora);

    @EntityGraph(attributePaths = {"mesa", "cliente"})
    @Query("""
            select r from Reserva r
            where r.estado = :estado and r.fecha = :fecha
              and (r.fecha < :hoy or (r.fecha = :hoy and r.hora < :ahora))
            order by r.hora, r.id
            """)
    List<Reserva> buscarFinalizadasDelDia(@Param("estado") EstadoReserva estado, @Param("fecha") LocalDate fecha,
                                          @Param("hoy") LocalDate hoy, @Param("ahora") LocalTime ahora);

    // Una reserva con su mesa y su cliente
    @EntityGraph(attributePaths = {"mesa", "cliente"})
    Optional<Reserva> findConDetalleById(Long id);
}
