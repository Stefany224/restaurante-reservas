package cl.ipss.restaurante.entity;

import cl.ipss.restaurante.enums.UbicacionMesa;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

// Entidad Mesa, representa una fila de la tabla mesa
@Entity
@Table(name = "mesa")
public class Mesa {

    // Id autoincremental que genera PostgreSQL
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Numero de la mesa, no se puede repetir
    @Column(nullable = false, unique = true)
    private Integer numero;

    // Maximo de personas que caben
    @Column(nullable = false)
    private Integer capacidad;

    // La ubicacion se guarda como texto para que la base sea legible
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private UbicacionMesa ubicacion;

    // Una mesa inactiva no se ofrece para reservar
    @Column(nullable = false)
    private boolean activa = true;

    // Fechas de creacion y ultima modificacion
    @Column(name = "creado_en", nullable = false, updatable = false)
    private LocalDateTime creadoEn;

    @Column(name = "actualizado_en", nullable = false)
    private LocalDateTime actualizadoEn;

    // Constructor vacio que exige JPA
    public Mesa() {
    }

    // Getters y setters de la entidad
    public Long getId() {
        return id;
    }

    public Integer getNumero() {
        return numero;
    }

    public Integer getCapacidad() {
        return capacidad;
    }

    public UbicacionMesa getUbicacion() {
        return ubicacion;
    }

    public boolean isActiva() {
        return activa;
    }

    public LocalDateTime getCreadoEn() {
        return creadoEn;
    }

    public LocalDateTime getActualizadoEn() {
        return actualizadoEn;
    }

    public void setNumero(Integer numero) {
        this.numero = numero;
    }

    public void setCapacidad(Integer capacidad) {
        this.capacidad = capacidad;
    }

    public void setUbicacion(UbicacionMesa ubicacion) {
        this.ubicacion = ubicacion;
    }

    public void setActiva(boolean activa) {
        this.activa = activa;
    }

    // Se ejecuta solo justo antes del INSERT
    @PrePersist
    void antesDeGuardar() {
        LocalDateTime ahora = LocalDateTime.now();
        this.creadoEn = ahora;
        this.actualizadoEn = ahora;
    }

    // Se ejecuta solo justo antes del UPDATE
    @PreUpdate
    void antesDeActualizar() {
        this.actualizadoEn = LocalDateTime.now();
    }
}
