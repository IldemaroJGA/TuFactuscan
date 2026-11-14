package com.tufactuscan.usuario;

import com.tufactuscan.usuario.dto.UsuarioResponse;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Endpoints de gestión de usuarios. Todos son exclusivos del rol ADMIN.
 *
 * <p>Uso: {@code @PreAuthorize("hasRole('ADMIN')")} en la clase se aplica a
 * todos sus métodos. Un EMPLEADO que llame aquí recibe un 403.</p>
 */
@RestController
@RequestMapping("/api/usuarios")
@PreAuthorize("hasRole('ADMIN')")
public class UsuarioController {

    private final UsuarioService usuarioService;

    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    /** GET /api/usuarios: lista los usuarios de la empresa del administrador. */
    @GetMapping
    public List<UsuarioResponse> listar() {
        return usuarioService.listarDeMiEmpresa();
    }
}
