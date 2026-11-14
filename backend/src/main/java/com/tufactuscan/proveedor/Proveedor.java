package com.tufactuscan.proveedor;

import com.tufactuscan.empresa.Empresa;
import jakarta.persistence.*;

import java.time.LocalDateTime;

/**
 * Entidad PROVEEDOR: quien emite las facturas y albaranes a una empresa.
 *
 * <p>Uso: tras el OCR, la app busca el proveedor por su NIF para asignarlo
 * automáticamente al documento. El NIF no se repite dentro de una misma
 * empresa (restricción {@code uq_proveedor_nif}).</p>
 *
 * <p>Tabla: {@code proveedor}.</p>
 */
@Entity
@Table(name = "proveedor")
public class Proveedor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Empresa propietaria de este proveedor. */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "empresa_id", nullable = false)
    private Empresa empresa;

    @Column(nullable = false, length = 150)
    private String nombre;

    @Column(length = 15)
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
    protected Proveedor() {
    }

    /** Crea un proveedor para una empresa. */
    public Proveedor(Empresa empresa, String nombre, String nif) {
        this.empresa = empresa;
        this.nombre = nombre;
        this.nif = nif;
    }

    @PrePersist
    void alCrear() {
        this.creadoEn = LocalDateTime.now();
    }

    // ----- Getters y setters -----

    public Long getId() { return id; }

    public Empresa getEmpresa() { return empresa; }

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
