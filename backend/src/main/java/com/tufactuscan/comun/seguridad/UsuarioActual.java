package com.tufactuscan.comun.seguridad;

import com.tufactuscan.usuario.Rol;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;

/**
 * Datos del usuario que hace la petición, sacados de su token JWT.
 *
 * <p>Uso: en cualquier servicio o controlador, {@code UsuarioActual.obtener()}
 * devuelve quién es, de qué empresa es y su rol. El {@code empresaId} sale
 * SIEMPRE del token firmado, nunca de lo que envía el cliente: así nadie
 * puede pedir datos de otra empresa (aislamiento, RNF03).</p>
 *
 * @param id        id del usuario
 * @param empresaId id de su empresa
 * @param rol       ADMIN o EMPLEADO
 * @param email     email del usuario
 */
public record UsuarioActual(Long id, Long empresaId, Rol rol, String email) {

    /** Indica si el usuario es administrador. */
    public boolean esAdmin() {
        return rol == Rol.ADMIN;
    }

    /**
     * Obtiene el usuario de la petición en curso.
     *
     * @throws IllegalStateException si se llama en una ruta sin autenticar
     */
    public static UsuarioActual obtener() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof Jwt jwt)) {
            throw new IllegalStateException("No hay un usuario autenticado en esta petición");
        }
        return desdeJwt(jwt);
    }

    /** Convierte los datos (claims) del token en un UsuarioActual. */
    public static UsuarioActual desdeJwt(Jwt jwt) {
        return new UsuarioActual(
                Long.valueOf(jwt.getSubject()),
                ((Number) jwt.getClaim(TokenService.CLAIM_EMPRESA)).longValue(),
                Rol.valueOf(jwt.getClaimAsString(TokenService.CLAIM_ROL)),
                jwt.getClaimAsString(TokenService.CLAIM_EMAIL));
    }
}
