package com.tufactuscan.comun.seguridad;

import com.tufactuscan.usuario.Usuario;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

/**
 * Crea los tokens JWT que se entregan al iniciar sesión.
 *
 * <p>Uso: {@code tokenService.generar(usuario)} devuelve un {@link TokenGenerado}
 * con el token firmado (HS256) y su fecha de caducidad. El token lleva dentro
 * el id del usuario, su empresa, su rol y su email; la validación de los
 * tokens que llegan en cada petición la hace Spring Security automáticamente
 * (ver {@link SeguridadConfig}).</p>
 */
@Service
public class TokenService {

    /** Nombres de los datos (claims) propios que guardamos en el token. */
    public static final String CLAIM_EMPRESA = "empresaId";
    public static final String CLAIM_ROL = "rol";
    public static final String CLAIM_EMAIL = "email";

    private static final String EMISOR = "tufactuscan";

    private final JwtEncoder encoder;
    private final long expiracionHoras;

    public TokenService(JwtEncoder encoder,
                        @Value("${tufactuscan.jwt.expiracion-horas}") long expiracionHoras) {
        this.encoder = encoder;
        this.expiracionHoras = expiracionHoras;
    }

    /** Token generado y momento en que deja de ser válido. */
    public record TokenGenerado(String token, Instant expiraEn) {
    }

    /** Genera un token firmado para el usuario indicado. */
    public TokenGenerado generar(Usuario usuario) {
        Instant ahora = Instant.now();
        Instant expira = ahora.plus(expiracionHoras, ChronoUnit.HOURS);

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(EMISOR)
                .issuedAt(ahora)
                .expiresAt(expira)
                .subject(String.valueOf(usuario.getId()))
                .claim(CLAIM_EMPRESA, usuario.getEmpresa().getId())
                .claim(CLAIM_ROL, usuario.getRol().name())
                .claim(CLAIM_EMAIL, usuario.getEmail())
                .build();

        JwsHeader cabecera = JwsHeader.with(MacAlgorithm.HS256).build();
        String token = encoder.encode(JwtEncoderParameters.from(cabecera, claims)).getTokenValue();
        return new TokenGenerado(token, expira);
    }
}
