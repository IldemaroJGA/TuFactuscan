package com.tufactuscan.auth.dto;

import com.tufactuscan.usuario.dto.UsuarioResponse;

import java.time.Instant;

/**
 * Respuesta de un login correcto.
 *
 * <p>Uso: el cliente guarda {@code token} y lo envía en cada petición con la
 * cabecera {@code Authorization: Bearer <token>}. Cuando llega
 * {@code expiraEn}, debe volver a iniciar sesión.</p>
 *
 * @param token   JWT firmado
 * @param tipo    siempre "Bearer"
 * @param expiraEn fecha y hora de caducidad (UTC)
 * @param usuario datos del usuario que ha iniciado sesión
 */
public record LoginResponse(String token, String tipo, Instant expiraEn, UsuarioResponse usuario) {
}
