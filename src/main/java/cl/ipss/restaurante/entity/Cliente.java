package cl.ipss.restaurante.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

// Entidad Cliente, representa una fila de la tabla cliente
@Entity
@Table(name = "cliente")
public class Cliente {

    // Id autoincremental que genera PostgreSQL
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Nombre del cliente
    @Column(nullable = false, length = 100)
    private String nombre;

    // El correo es unico, si alguien vuelve a reservar con el mismo correo se reutiliza su registro
    @Column(nullable = false, length = 120, unique = true)
    private String email;

    // Telefono con formato X XXXX XXXX
    @Column(nullable = false, length = 20)
    private String telefono;

    // Fecha en que se registro
    @Column(name = "creado_en", nullable = false, updatable = false)
    private LocalDateTime creadoEn;

    // Constructor vacio que exige JPA
    public Cliente() {
    }

    // Getters y setters de la entidad
    public Long getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public String getEmail() {
        return email;
    }

    public String getTelefono() {
        return telefono;
    }

    public LocalDateTime getCreadoEn() {
        return creadoEn;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    // Se ejecuta solo justo antes del INSERT
    @PrePersist
    void antesDeGuardar() {
        this.creadoEn = LocalDateTime.now();
    }
}
