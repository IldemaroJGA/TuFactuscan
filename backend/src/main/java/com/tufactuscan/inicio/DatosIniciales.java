package com.tufactuscan.inicio;

import com.tufactuscan.empresa.Empresa;
import com.tufactuscan.empresa.EmpresaRepository;
import com.tufactuscan.usuario.Rol;
import com.tufactuscan.usuario.Usuario;
import com.tufactuscan.usuario.UsuarioRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Crea los usuarios de prueba al arrancar el backend.
 *
 * <p>Uso: los usuarios no van en el script SQL porque su contraseña debe
 * guardarse cifrada con BCrypt, y eso lo hace Java. Solo se crean si no
 * existen, así que reiniciar el backend no los duplica.</p>
 *
 * <p>Se desactiva poniendo {@code tufactuscan.datos-iniciales=false}
 * (o la variable de entorno {@code DATOS_INICIALES=false}).</p>
 *
 * <p>Usuarios creados (solo para desarrollo):</p>
 * <ul>
 *   <li>admin@tallergotera.es / Admin1234! (ADMIN, empresa B12345678)</li>
 *   <li>empleado@tallergotera.es / Empleado1234! (EMPLEADO, empresa B12345678)</li>
 *   <li>lahuerta@email.es / Huerta1234! (ADMIN, empresa 12345678Z)</li>
 * </ul>
 */
@Component
@ConditionalOnProperty(name = "tufactuscan.datos-iniciales", havingValue = "true")
public class DatosIniciales implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(DatosIniciales.class);

    private final EmpresaRepository empresas;
    private final UsuarioRepository usuarios;
    private final PasswordEncoder passwordEncoder;

    /** Spring inyecta las dependencias por constructor. */
    public DatosIniciales(EmpresaRepository empresas, UsuarioRepository usuarios, PasswordEncoder passwordEncoder) {
        this.empresas = empresas;
        this.usuarios = usuarios;
        this.passwordEncoder = passwordEncoder;
    }

    /** Se ejecuta una vez, cuando la aplicación ya ha arrancado. */
    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        crearUsuarioSiNoExiste("B12345678", "Ildemaro Gotera", "admin@tallergotera.es", "Admin1234!", Rol.ADMIN);
        crearUsuarioSiNoExiste("B12345678", "Laura Martín", "empleado@tallergotera.es", "Empleado1234!", Rol.EMPLEADO);
        crearUsuarioSiNoExiste("12345678Z", "Carmen Ruiz", "lahuerta@email.es", "Huerta1234!", Rol.ADMIN);
    }

    /**
     * Crea un usuario en la empresa indicada si su email no existe todavía.
     * Si la empresa no existe (no se cargaron los datos de prueba), lo avisa y no hace nada.
     */
    private void crearUsuarioSiNoExiste(String nifEmpresa, String nombre, String email,
                                        String passwordPlano, Rol rol) {
        if (usuarios.existsByEmail(email)) {
            return;
        }
        Empresa empresa = empresas.findByNif(nifEmpresa).orElse(null);
        if (empresa == null) {
            log.warn("No existe la empresa {}: no se crea el usuario {}", nifEmpresa, email);
            return;
        }
        String hash = passwordEncoder.encode(passwordPlano);
        usuarios.save(new Usuario(empresa, nombre, email, hash, rol));
        log.info("Usuario de prueba creado: {} ({})", email, rol);
    }
}
