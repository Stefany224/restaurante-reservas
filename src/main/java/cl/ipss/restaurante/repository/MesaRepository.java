package cl.ipss.restaurante.repository;

import cl.ipss.restaurante.entity.Mesa;
import cl.ipss.restaurante.enums.EstadoReserva;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

// MesaRepository accede a la tabla mesa, JpaRepository ya trae save, findById, findAll y delete
@Repository
public interface MesaRepository extends JpaRepository<Mesa, Long> {

    // Revisan si el numero de mesa ya existe, la segunda ignora la mesa que se esta editando
    boolean existsByNumero(Integer numero);

    boolean existsByNumeroAndIdNot(Integer numero, Long id);

    // Todas las mesas ordenadas por numero
    List<Mesa> findAllByOrderByNumeroAsc();

    // Mesas libres para un turno, activas, con capacidad suficiente y sin una reserva activa ese dia a esa hora, al modificar se excluye la propia reserva para que su mesa salga libre
    @Query("""
            select m from Mesa m
            where m.activa = true
              and m.capacidad >= :personas
              and m.id not in (
                  select r.mesa.id from Reserva r
                  where r.fecha = :fecha and r.hora = :hora and r.estado = :estado
                    and r.id <> :excluirReservaId)
            order by m.capacidad, m.numero
            """)
    List<Mesa> buscarDisponibles(@Param("fecha") LocalDate fecha,
                                 @Param("hora") LocalTime hora,
                                 @Param("personas") int personas,
                                 @Param("estado") EstadoReserva estado,
                                 @Param("excluirReservaId") Long excluirReservaId);

    // Lee la mesa bloqueandola, asi si dos personas reservan la misma mesa al mismo tiempo la segunda espera y despues ve que ya esta ocupada
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select m from Mesa m where m.id = :id")
    Optional<Mesa> buscarParaActualizar(@Param("id") Long id);
}
