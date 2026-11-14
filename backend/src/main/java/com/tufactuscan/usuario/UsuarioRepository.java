package com.tufactuscan.usuario;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * Acceso a datos de la tabla {@code usuario}.
 *
 * <p>Uso: el login buscará al usuario por email; la gestión de usuarios
 * (solo ADMIN) listará los de su empresa.</p>
 */
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    /** Busca un usuario por email (para iniciar sesión). */
    Optional<Usuario> findByEmail(String email);

    /** Comprueba si ya existe un email (para no duplicar al dar de alta). */
    boolean existsByEmail(String email);

    /** Lista los usuarios de una empresa, ordenados por nombre. */
    List<Usuario> findByEmpresaIdOrderByNombreAsc(Long empresaId);
}
