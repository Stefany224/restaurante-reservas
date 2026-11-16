package cl.ipss.restaurante.entity;

import cl.ipss.restaurante.enums.EstadoReserva;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

// Entidad Reserva, une a un cliente con una mesa en un dia y hora
@Entity
@Table(name = "reserva")
public class Reserva {

    // Id autoincremental que genera PostgreSQL
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Una mesa puede tener muchas reservas, se carga LAZY para traerla solo cuando se pide
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "mesa_id", nullable = false)
    private Mesa mesa;

    // Un cliente puede tener muchas reservas
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cliente_id", nullable = false)
    private Cliente cliente;

    // Dia y hora del turno reservado
    @Column(nullable = false)
    private LocalDate fecha;

    @Column(nullable = false)
    private LocalTime hora;

    // Cantidad de personas, nunca mayor a la capacidad de la mesa
    @Column(nullable = false)
    private Integer personas;

    // Comentario opcional del cliente
    @Column(length = 250)
    private String comentario;

    // ACTIVA o CANCELADA, guardado como texto
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private EstadoReserva estado;

    // Fechas de creacion y ultima modificacion
    @Column(name = "creado_en", nullable = false, updatable = false)
    private LocalDateTime creadoEn;

    @Column(name = "actualizado_en", nullable = false)
    private LocalDateTime actualizadoEn;

    // Constructor vacio que exige JPA
    public Reserva() {
    }

    // Getters y setters de la entidad
    public Long getId() {
        return id;
    }

    public Mesa getMesa() {
        return mesa;
    }

    public Cliente getCliente() {
        return cliente;
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public LocalTime getHora() {
        return hora;
    }

    public Integer getPersonas() {
        return personas;
    }

    public String getComentario() {
        return comentario;
    }

    public EstadoReserva getEstado() {
        return estado;
    }

    public LocalDateTime getCreadoEn() {
        return creadoEn;
    }

    public LocalDateTime getActualizadoEn() {
        return actualizadoEn;
    }

    public void setMesa(Mesa mesa) {
        this.mesa = mesa;
    }

    public void setCliente(Cliente cliente) {
        this.cliente = cliente;
    }

    public void setFecha(LocalDate fecha) {
        this.fecha = fecha;
    }

    public void setHora(LocalTime hora) {
        this.hora = hora;
    }

    public void setPersonas(Integer personas) {
        this.personas = personas;
    }

    public void setComentario(String comentario) {
        this.comentario = comentario;
    }

    public void setEstado(EstadoReserva estado) {
        this.estado = estado;
    }

    // Dice si el dia y la hora de la reserva ya pasaron
    public boolean yaPaso() {
        return LocalDateTime.of(fecha, hora).isBefore(LocalDateTime.now());
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
