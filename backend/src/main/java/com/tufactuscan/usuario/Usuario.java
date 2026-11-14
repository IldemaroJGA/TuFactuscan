package com.tufactuscan.usuario;

import com.tufactuscan.empresa.Empresa;
import jakarta.persistence.*;

import java.time.LocalDateTime;

/**
 * Entidad USUARIO: persona que usa la app o el panel web.
 *
 * <p>Uso: identifica quién inicia sesión, a qué empresa pertenece y qué
 * puede hacer según su {@link Rol}. La contraseña se guarda SIEMPRE como
 * hash BCrypt en {@code passwordHash}, nunca en texto plano (RNF01).</p>
 *
 * <p>Tabla: {@code usuario}.</p>
 */
@Entity
@Table(name = "usuario")
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Empresa a la que pertenece. LAZY: solo se carga de la BD si se usa. */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "empresa_id", nullable = false)
    private Empresa empresa;

    @Column(nullable = false, length = 100)
    private String nombre;

    /** Email de acceso; único en todo el sistema. */
    @Column(nullable = false, length = 150, unique = true)
    private String email;

    /** Hash BCrypt de la contraseña (60 caracteres). */
    @Column(name = "password_hash", nullable = false, length = 100)
    private String passwordHash;

    /** Se guarda como texto ('ADMIN' / 'EMPLEADO'), igual que el ENUM de MySQL. */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Rol rol = Rol.EMPLEADO;

    /** Un usuario desactivado no puede iniciar sesión (no se borra para conservar el historial). */
    @Column(nullable = false)
    private boolean activo = true;

    @Column(name = "creado_en", nullable = false, updatable = false)
    private LocalDateTime creadoEn;

    @Column(name = "ultimo_acceso")
    private LocalDateTime ultimoAcceso;

    /** Constructor vacío obligatorio para JPA. */
    protected Usuario() {
    }

    /**
     * Crea un usuario.
     *
     * @param passwordHash contraseña ya cifrada con BCrypt (no la contraseña original)
     */
    public Usuario(Empresa empresa, String nombre, String email, String passwordHash, Rol rol) {
        this.empresa = empresa;
        this.nombre = nombre;
        this.email = email;
        this.passwordHash = passwordHash;
        this.rol = rol;
    }

    @PrePersist
    void alCrear() {
        this.creadoEn = LocalDateTime.now();
    }

    /** Indica si el usuario es administrador de su empresa. */
    public boolean esAdmin() {
        return rol == Rol.ADMIN;
    }

    // ----- Getters y setters -----

    public Long getId() { return id; }

    public Empresa getEmpresa() { return empresa; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }

    public Rol getRol() { return rol; }
    public void setRol(Rol rol) { this.rol = rol; }

    public boolean isActivo() { return activo; }
    public void setActivo(boolean activo) { this.activo = activo; }

    public LocalDateTime getCreadoEn() { return creadoEn; }

    public LocalDateTime getUltimoAcceso() { return ultimoAcceso; }
    public void setUltimoAcceso(LocalDateTime ultimoAcceso) { this.ultimoAcceso = ultimoAcceso; }
}
