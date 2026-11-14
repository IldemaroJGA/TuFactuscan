package com.tufactuscan.empresa;

import jakarta.persistence.*;

import java.time.LocalDateTime;

/**
 * Entidad EMPRESA: cada cliente de TuFactuScan (autónomo o pyme).
 *
 * <p>Uso: es la raíz de los datos. Usuarios, proveedores y documentos
 * pertenecen siempre a una empresa, y la API filtra todo por ella para que
 * una empresa nunca vea los datos de otra (requisito RNF03).</p>
 *
 * <p>Tabla: {@code empresa} (ver database/01_schema.sql).</p>
 */
@Entity
@Table(name = "empresa")
public class Empresa {

    /** Clave primaria autoincremental. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String nombre;

    /** NIF/CIF de la empresa; único en todo el sistema. */
    @Column(nullable = false, length = 15, unique = true)
    private String nif;

    @Column(length = 150)
    private String email;

    @Column(length = 20)
    private String telefono;

    @Column(length = 255)
    private String direccion;

    @Column(name = "creado_en", nullable = false, updatable = false)
    private LocalDateTime creadoEn;

    /** Constructor vacío obligatorio para JPA. */
    protected Empresa() {
    }

    /** Crea una empresa con los datos mínimos. */
    public Empresa(String nombre, String nif) {
        this.nombre = nombre;
        this.nif = nif;
    }

    /** Rellena la fecha de creación justo antes de insertar en la base de datos. */
    @PrePersist
    void alCrear() {
        this.creadoEn = LocalDateTime.now();
    }

    // ----- Getters y setters -----

    public Long getId() { return id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getNif() { return nif; }
    public void setNif(String nif) { this.nif = nif; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }

    public String getDireccion() { return direccion; }
    public void setDireccion(String direccion) { this.direccion = direccion; }

    public LocalDateTime getCreadoEn() { return creadoEn; }
}
