package com.tufactuscan.comun.error;

import java.util.Map;

/**
 * Formato común de todas las respuestas de error de la API.
 *
 * <p>Uso: la app y la web leen siempre los mismos campos:</p>
 * <pre>
 * { "status": 400, "error": "VALIDACION", "mensaje": "Revisa los datos",
 *   "campos": { "email": "no es un email válido" } }
 * </pre>
 *
 * @param status  código HTTP
 * @param error   código corto y fijo, útil para el programa (VALIDACION, NO_ENCONTRADO...)
 * @param mensaje texto legible para mostrar al usuario
 * @param campos  errores por campo (solo en errores de validación; si no, vacío)
 */
public record ApiError(int status, String error, String mensaje, Map<String, String> campos) {

    /** Crea un error sin detalle por campos. */
    public static ApiError de(int status, String error, String mensaje) {
        return new ApiError(status, error, mensaje, Map.of());
    }
}
