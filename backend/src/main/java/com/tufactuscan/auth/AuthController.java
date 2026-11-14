package com.tufactuscan.auth;

import com.tufactuscan.auth.dto.LoginRequest;
import com.tufactuscan.auth.dto.LoginResponse;
import com.tufactuscan.usuario.dto.UsuarioResponse;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

/**
 * Endpoints de autenticación.
 *
 * <ul>
 *   <li>{@code POST /api/auth/login} (público): devuelve el token.</li>
 *   <li>{@code GET /api/auth/me} (con token): datos del usuario y su empresa.</li>
 * </ul>
 *
 * <p>Uso: el controlador solo recibe y devuelve datos; la lógica está en
 * {@link AuthService}. {@code @Valid} activa las validaciones de
 * {@link LoginRequest}; si fallan, responde 400 automáticamente.</p>
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    /** Inicia sesión con email y contraseña. */
    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest peticion) {
        return authService.login(peticion);
    }

    /** Devuelve el usuario autenticado (sirve para comprobar que el token es válido). */
    @GetMapping("/me")
    public UsuarioResponse yo() {
        return authService.yo();
    }
}
