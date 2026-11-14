package com.tufactuscan.comun.seguridad;

import com.nimbusds.jose.jwk.source.ImmutableSecret;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;

/**
 * Configuración central de seguridad de la API.
 *
 * <p>Uso: define qué rutas son públicas y cuáles exigen token, cómo se
 * firman y validan los JWT y cómo se traduce el rol del token a permisos de
 * Spring ({@code ROLE_ADMIN}, {@code ROLE_EMPLEADO}).</p>
 *
 * <p>{@link EnableMethodSecurity} permite proteger métodos concretos con
 * {@code @PreAuthorize("hasRole('ADMIN')")}.</p>
 */
@Configuration
@EnableMethodSecurity
public class SeguridadConfig {

    /** Rutas a las que se puede acceder sin token. */
    private static final String[] RUTAS_PUBLICAS = {
            "/api/auth/login",
            "/api/estado"
    };

    private final SecretKey clave;

    /**
     * Crea la clave HMAC a partir del secreto configurado.
     *
     * @throws IllegalStateException si el secreto es demasiado corto (HS256 exige 256 bits)
     */
    public SeguridadConfig(@Value("${tufactuscan.jwt.secreto}") String secreto) {
        byte[] bytes = secreto.getBytes(StandardCharsets.UTF_8);
        if (bytes.length < 32) {
            throw new IllegalStateException("tufactuscan.jwt.secreto debe tener al menos 32 caracteres");
        }
        this.clave = new SecretKeySpec(bytes, "HmacSHA256");
    }

    /**
     * Cadena de filtros de seguridad que se aplica a cada petición HTTP.
     */
    @Bean
    public SecurityFilterChain filtros(HttpSecurity http, RespuestasSeguridad respuestas) throws Exception {
        http
                // API sin sesiones ni cookies: el token viaja en cada petición, así que CSRF no aplica
                .csrf(csrf -> csrf.disable())
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // Reglas de acceso por ruta (las de rol concreto van en los métodos con @PreAuthorize)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.POST, "/api/auth/login").permitAll()
                        .requestMatchers(RUTAS_PUBLICAS).permitAll()
                        .requestMatchers("/api/**").authenticated()
                        .anyRequest().permitAll())

                // Valida el token "Authorization: Bearer ..." de cada petición
                .oauth2ResourceServer(oauth -> oauth
                        .jwt(jwt -> jwt.jwtAuthenticationConverter(convertidorRoles()))
                        .authenticationEntryPoint(respuestas.noAutenticado())
                        .accessDeniedHandler(respuestas.accesoDenegado()))

                // Respuestas JSON para 401 y 403
                .exceptionHandling(e -> e
                        .authenticationEntryPoint(respuestas.noAutenticado())
                        .accessDeniedHandler(respuestas.accesoDenegado()));

        return http.build();
    }

    /** Firma los tokens al iniciar sesión (HS256 con la clave secreta). */
    @Bean
    public JwtEncoder jwtEncoder() {
        return new NimbusJwtEncoder(new ImmutableSecret<>(clave));
    }

    /** Comprueba la firma y la caducidad de los tokens que llegan. */
    @Bean
    public JwtDecoder jwtDecoder() {
        return NimbusJwtDecoder.withSecretKey(clave)
                .macAlgorithm(MacAlgorithm.HS256)
                .build();
    }

    /**
     * Traduce el claim "rol" del token (ADMIN / EMPLEADO) a la autoridad
     * de Spring "ROLE_ADMIN" / "ROLE_EMPLEADO", que es lo que comprueba
     * {@code hasRole('ADMIN')}.
     */
    private JwtAuthenticationConverter convertidorRoles() {
        JwtGrantedAuthoritiesConverter roles = new JwtGrantedAuthoritiesConverter();
        roles.setAuthoritiesClaimName(TokenService.CLAIM_ROL);
        roles.setAuthorityPrefix("ROLE_");

        JwtAuthenticationConverter convertidor = new JwtAuthenticationConverter();
        convertidor.setJwtGrantedAuthoritiesConverter(roles);
        return convertidor;
    }
}
