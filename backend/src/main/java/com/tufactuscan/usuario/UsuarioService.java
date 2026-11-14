package com.tufactuscan.usuario;

import com.tufactuscan.comun.seguridad.UsuarioActual;
import com.tufactuscan.usuario.dto.UsuarioResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Lógica de gestión de usuarios.
 *
 * <p>Uso: en el bloque 2 solo lista los usuarios de la empresa (sirve para
 * probar los roles). El alta, la edición y la desactivación se añaden en el
 * bloque 3.</p>
 */
@Service
public class UsuarioService {

    private final UsuarioRepository usuarios;

    public UsuarioService(UsuarioRepository usuarios) {
        this.usuarios = usuarios;
    }

    /** Usuarios de la empresa del usuario autenticado (nunca de otra empresa). */
    @Transactional(readOnly = true)
    public List<UsuarioResponse> listarDeMiEmpresa() {
        Long empresaId = UsuarioActual.obtener().empresaId();
        return usuarios.findByEmpresaIdOrderByNombreAsc(empresaId).stream()
                .map(UsuarioResponse::desde)
                .toList();
    }
}
