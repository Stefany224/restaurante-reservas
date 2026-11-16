package cl.ipss.restaurante.service;

import cl.ipss.restaurante.dto.MesaForm;
import cl.ipss.restaurante.entity.Mesa;
import cl.ipss.restaurante.enums.UbicacionMesa;
import cl.ipss.restaurante.exception.DatoDuplicadoException;
import cl.ipss.restaurante.exception.MesaConReservasException;
import cl.ipss.restaurante.exception.RecursoNoEncontradoException;
import cl.ipss.restaurante.mapper.MesaMapper;
import cl.ipss.restaurante.repository.MesaRepository;
import cl.ipss.restaurante.repository.ReservaRepository;
import cl.ipss.restaurante.service.impl.MesaServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

// Pruebas unitarias del CRUD de mesas con los repositorios simulados
@ExtendWith(MockitoExtension.class)
class MesaServiceImplTest {

    // Repositorios simulados
    @Mock
    private MesaRepository mesaRepository;
    @Mock
    private ReservaRepository reservaRepository;

    // Service que se prueba
    private MesaServiceImpl service;

    // Antes de cada prueba se arma el service con los repositorios simulados
    @BeforeEach
    void preparar() {
        service = new MesaServiceImpl(mesaRepository, reservaRepository, new MesaMapper());
    }

    // Mesa de prueba
    private Mesa mesa() {
        Mesa m = new Mesa();
        ReflectionTestUtils.setField(m, "id", 1L);
        m.setNumero(3);
        m.setCapacidad(4);
        m.setUbicacion(UbicacionMesa.TERRAZA);
        return m;
    }

    // Pruebas de las reglas de mesas
    @Test
    void noPermiteDosMesasConElMismoNumero() {
        MesaForm f = new MesaForm();
        f.setNumero(3);
        f.setCapacidad(4);
        f.setUbicacion(UbicacionMesa.SALON);
        when(mesaRepository.existsByNumero(3)).thenReturn(true);

        assertThrows(DatoDuplicadoException.class, () -> service.crear(f));
        verify(mesaRepository, never()).save(any());
    }

    @Test
    void noEliminaUnaMesaConReservas() {
        Mesa m = mesa();
        when(mesaRepository.findById(1L)).thenReturn(Optional.of(m));
        when(reservaRepository.existsByMesaId(1L)).thenReturn(true);

        assertThrows(MesaConReservasException.class, () -> service.eliminar(1L));
        verify(mesaRepository, never()).delete(m);
    }

    @Test
    void eliminaUnaMesaSinReservas() {
        Mesa m = mesa();
        when(mesaRepository.findById(1L)).thenReturn(Optional.of(m));
        when(reservaRepository.existsByMesaId(1L)).thenReturn(false);

        service.eliminar(1L);

        verify(mesaRepository).delete(m);
    }

    @Test
    void obtenerLanzaErrorSiNoExiste() {
        when(mesaRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(RecursoNoEncontradoException.class, () -> service.obtener(99L));
    }
}
