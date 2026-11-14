package com.tufactuscan.auth;

import com.tufactuscan.auth.dto.LoginRequest;
import com.tufactuscan.auth.dto.LoginResponse;
import com.tufactuscan.comun.error.CredencialesInvalidasException;
import com.tufactuscan.comun.error.NoEncontradoException;
import com.tufactuscan.comun.seguridad.TokenService;
import com.tufactuscan.comun.seguridad.UsuarioActual;
import com.tufactuscan.usuario.Usuario;
import com.tufactuscan.usuario.UsuarioRepository;
import com.tufactuscan.usuario.dto.UsuarioResponse;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Locale;

/**
 * Lógica de autenticación: comprobar credenciales y emitir el token.
 *
 * <p>Uso: lo llama {@link AuthController}. Separar la lógica del controlador
 * permite probarla sin HTTP y mantener el controlador pequeño.</p>
 */
@Service
public class AuthService {

    /**
     * Hash BCrypt de una contraseña cualquiera. Si el email no existe se
     * compara contra él igualmente, para que la respuesta tarde lo mismo y
     * no se pueda averiguar qué emails están registrados midiendo el tiempo.
     */
    private static final String HASH_FALSO =
            "$2a$10$7EqJtq98hPqEX7fNZaFWoOhi5BWXu7Z0O8d3gF9nC2cB2Q5V1wQ8a";

    private final UsuarioRepository usuarios;
    private final PasswordEncoder passwordEncoder;
    private final TokenService tokenService;

    public AuthService(UsuarioRepository usuarios, PasswordEncoder passwordEncoder, TokenService tokenService) {
        this.usuarios = usuarios;
        this.passwordEncoder = passwordEncoder;
        this.tokenService = tokenService;
    }

    /**
     * Comprueba email y contraseña y, si son correctos, devuelve un token.
     *
     * @throws CredencialesInvalidasException si el email no existe, la contraseña
     *         no coincide o el usuario está desactivado (mismo error en los tres casos)
     */
    @Transactional
    public LoginResponse login(LoginRequest peticion) {
        String email = peticion.email().trim().toLowerCase(Locale.ROOT);
        Usuario usuario = usuarios.findByEmail(email).orElse(null);

        String hash = (usuario != null) ? usuario.getPasswordHash() : HASH_FALSO;
        boolean passwordCorrecta = passwordEncoder.matches(peticion.password(), hash);

        if (usuario == null || !passwordCorrecta || !usuario.isActivo()) {
            throw new CredencialesInvalidasException();
        }

        usuario.setUltimoAcceso(LocalDateTime.now());   // se guarda al terminar la transacción
        TokenService.TokenGenerado token = tokenService.generar(usuario);
        return new LoginResponse(token.token(), "Bearer", token.expiraEn(), UsuarioResponse.desde(usuario));
    }

    /** Devuelve los datos del usuario que hace la petición (GET /api/auth/me). */
    @Transactional(readOnly = true)
    public UsuarioResponse yo() {
        UsuarioActual actual = UsuarioActual.obtener();
        Usuario usuario = usuarios.findById(actual.id())
                .orElseThrow(() -> new NoEncontradoException("Usuario no encontrado"));
        return UsuarioResponse.desde(usuario);
    }
}
