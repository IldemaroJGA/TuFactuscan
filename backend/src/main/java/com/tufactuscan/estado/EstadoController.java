package com.tufactuscan.estado;

import com.tufactuscan.documento.DocumentoRepository;
import com.tufactuscan.empresa.EmpresaRepository;
import com.tufactuscan.proveedor.ProveedorRepository;
import com.tufactuscan.usuario.UsuarioRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

/**
 * Endpoint de diagnóstico: comprueba que el backend funciona y que se
 * conecta a la base de datos.
 *
 * <p>Uso: abrir en el navegador {@code http://localhost:8080/api/estado}.
 * Devuelve cuántos registros hay en cada tabla principal. Es la prueba de
 * validación del bloque 1 de la fase 2.</p>
 *
 * <p>En el bloque 2 (seguridad) seguirá siendo público, pero sin datos
 * sensibles: solo totales.</p>
 */
@RestController
@RequestMapping("/api/estado")
public class EstadoController {

    private final EmpresaRepository empresas;
    private final UsuarioRepository usuarios;
    private final ProveedorRepository proveedores;
    private final DocumentoRepository documentos;

    public EstadoController(EmpresaRepository empresas, UsuarioRepository usuarios,
                            ProveedorRepository proveedores, DocumentoRepository documentos) {
        this.empresas = empresas;
        this.usuarios = usuarios;
        this.proveedores = proveedores;
        this.documentos = documentos;
    }

    /**
     * Respuesta del endpoint. Un {@code record} es una clase inmutable que
     * Java genera con constructor, getters, equals y toString; Spring la
     * convierte a JSON automáticamente.
     */
    public record EstadoResponse(String estado, String hora,
                                 long empresas, long usuarios, long proveedores, long documentos) {
    }

    /** GET /api/estado: devuelve el estado del servicio y los totales de cada tabla. */
    @GetMapping
    public EstadoResponse estado() {
        return new EstadoResponse(
                "OK",
                LocalDateTime.now().toString(),
                empresas.count(),
                usuarios.count(),
                proveedores.count(),
                documentos.count());
    }
}
