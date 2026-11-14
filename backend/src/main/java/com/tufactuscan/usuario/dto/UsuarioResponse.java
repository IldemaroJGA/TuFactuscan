package com.tufactuscan.usuario.dto;

import com.tufactuscan.usuario.Rol;
import com.tufactuscan.usuario.Usuario;

import java.time.LocalDateTime;

/**
 * Datos de un usuario que la API devuelve al cliente.
 *
 * <p>Uso: nunca se devuelve la entidad {@link Usuario} directamente, para no
 * exponer el hash de la contraseña ni relaciones internas. Este DTO (objeto de
 * transferencia de datos) solo lleva lo que el cliente necesita.</p>
 */
public record UsuarioResponse(
        Long id,
        String nombre,
        String email,
        Rol rol,
        boolean activo,
        Long empresaId,
        String empresaNombre,
        LocalDateTime ultimoAcceso) {

    /** Convierte la entidad en DTO. Debe llamarse dentro de una transacción (empresa es LAZY). */
    public static UsuarioResponse desde(Usuario u) {
        return new UsuarioResponse(
                u.getId(),
                u.getNombre(),
                u.getEmail(),
                u.getRol(),
                u.isActivo(),
                u.getEmpresa().getId(),
                u.getEmpresa().getNombre(),
                u.getUltimoAcceso());
    }
}
