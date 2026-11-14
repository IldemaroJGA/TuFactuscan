package com.tufactuscan.comun.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Configuración del cifrado de contraseñas.
 *
 * <p>Uso: declara un único {@link PasswordEncoder} (BCrypt) que se inyecta
 * donde haga falta cifrar o comprobar contraseñas: alta de usuarios, login
 * y datos iniciales. Tenerlo en un solo sitio permite cambiar el algoritmo
 * sin tocar el resto del código.</p>
 */
@Configuration
public class CifradoConfig {

    /** Coste 10 (por defecto): buen equilibrio entre seguridad y velocidad. */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
