package com.tufactuscan.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Datos que envía el cliente para iniciar sesión.
 *
 * <p>Uso: cuerpo JSON de {@code POST /api/auth/login}:
 * {@code {"email":"admin@tallergotera.es","password":"Admin1234!"}}.
 * Las anotaciones validan el formato antes de llegar al servicio.</p>
 */
public record LoginRequest(

        @NotBlank(message = "El email es obligatorio")
        @Email(message = "No es un email válido")
        @Size(max = 150)
        String email,

        @NotBlank(message = "La contraseña es obligatoria")
        @Size(max = 100)
        String password) {
}
