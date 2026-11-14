package com.tufactuscan.comun.seguridad;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * Respuestas JSON para los errores que detecta Spring Security antes de
 * llegar a los controladores.
 *
 * <p>Uso: {@link SeguridadConfig} las registra. Devuelven el mismo formato
 * que el resto de errores de la API (ver {@code ApiError}), en lugar de la
 * página HTML por defecto, para que la app Android pueda leerlas.</p>
 *
 * <ul>
 *   <li>401: no hay token, ha caducado o su firma no es válida.</li>
 *   <li>403: el token es válido pero el rol no permite la acción.</li>
 * </ul>
 */
@Component
public class RespuestasSeguridad {

    /** Se ejecuta cuando la petición no está autenticada (401). */
    public AuthenticationEntryPoint noAutenticado() {
        return (peticion, respuesta, excepcion) ->
                escribir(respuesta, 401, "NO_AUTENTICADO",
                        "Debes iniciar sesión o tu sesión ha caducado");
    }

    /** Se ejecuta cuando el usuario no tiene permiso (403). */
    public AccessDeniedHandler accesoDenegado() {
        return (peticion, respuesta, excepcion) ->
                escribir(respuesta, 403, "SIN_PERMISO",
                        "No tienes permiso para realizar esta acción");
    }

    /**
     * Escribe un JSON sencillo en la respuesta. Los textos son fijos y no
     * contienen comillas, así que se puede construir a mano sin riesgo.
     */
    private void escribir(HttpServletResponse respuesta, int status, String error, String mensaje)
            throws IOException {
        respuesta.setStatus(status);
        respuesta.setContentType(MediaType.APPLICATION_JSON_VALUE);
        respuesta.setCharacterEncoding(StandardCharsets.UTF_8.name());
        respuesta.getWriter().write(
                "{\"status\":" + status + ",\"error\":\"" + error + "\",\"mensaje\":\"" + mensaje + "\"}");
    }
}
