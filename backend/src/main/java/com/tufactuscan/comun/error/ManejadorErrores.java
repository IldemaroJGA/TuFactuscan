package com.tufactuscan.comun.error;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.servlet.resource.NoResourceFoundException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Manejador global de errores de la API.
 *
 * <p>Uso: atrapa las excepciones que salen de cualquier controlador y las
 * convierte en un {@link ApiError} con el código HTTP adecuado. Así los
 * controladores solo lanzan excepciones y nunca se devuelven trazas de Java
 * ni detalles internos al cliente.</p>
 */
@RestControllerAdvice
public class ManejadorErrores {

    private static final Logger log = LoggerFactory.getLogger(ManejadorErrores.class);

    /** 400: algún campo no cumple las validaciones (@NotBlank, @Email...). */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> validacion(MethodArgumentNotValidException ex) {
        Map<String, String> campos = new LinkedHashMap<>();
        ex.getBindingResult().getFieldErrors()
                .forEach(e -> campos.putIfAbsent(e.getField(), e.getDefaultMessage()));
        ApiError error = new ApiError(400, "VALIDACION", "Revisa los datos enviados", campos);
        return ResponseEntity.badRequest().body(error);
    }

    /** 400: el cuerpo de la petición no es un JSON válido o tiene tipos incorrectos. */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiError> jsonIncorrecto(HttpMessageNotReadableException ex) {
        return responder(HttpStatus.BAD_REQUEST, "PETICION_INCORRECTA", "El formato de los datos no es válido");
    }

    /** 401: login fallido. */
    @ExceptionHandler(CredencialesInvalidasException.class)
    public ResponseEntity<ApiError> credenciales(CredencialesInvalidasException ex) {
        return responder(HttpStatus.UNAUTHORIZED, "CREDENCIALES_INVALIDAS", ex.getMessage());
    }

    /** 403: el rol no permite la acción (lanzada por @PreAuthorize). */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiError> sinPermiso(AccessDeniedException ex) {
        return responder(HttpStatus.FORBIDDEN, "SIN_PERMISO", "No tienes permiso para realizar esta acción");
    }

    /** 404: recurso inexistente o de otra empresa. */
    @ExceptionHandler(NoEncontradoException.class)
    public ResponseEntity<ApiError> noEncontrado(NoEncontradoException ex) {
        return responder(HttpStatus.NOT_FOUND, "NO_ENCONTRADO", ex.getMessage());
    }

    /** 404: la ruta no existe (p. ej. /api/loquesea). */
    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ApiError> rutaInexistente(NoResourceFoundException ex) {
        return responder(HttpStatus.NOT_FOUND, "NO_ENCONTRADO", "La ruta solicitada no existe");
    }

    /** 405: la ruta existe pero no admite ese método (p. ej. GET en vez de POST). */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ApiError> metodoNoPermitido(HttpRequestMethodNotSupportedException ex) {
        return responder(HttpStatus.METHOD_NOT_ALLOWED, "METODO_NO_PERMITIDO",
                "Método HTTP no permitido en esta ruta");
    }

    /** 409: conflicto con datos existentes o con una regla de negocio. */
    @ExceptionHandler(ConflictoException.class)
    public ResponseEntity<ApiError> conflicto(ConflictoException ex) {
        return responder(HttpStatus.CONFLICT, "CONFLICTO", ex.getMessage());
    }

    /** 500: cualquier error no previsto. Se registra en el log, pero al cliente solo se le da un mensaje genérico. */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> inesperado(Exception ex) {
        log.error("Error no controlado", ex);
        return responder(HttpStatus.INTERNAL_SERVER_ERROR, "ERROR_INTERNO",
                "Se ha producido un error. Inténtalo de nuevo más tarde");
    }

    /** Construye la respuesta con el formato común. */
    private ResponseEntity<ApiError> responder(HttpStatus status, String codigo, String mensaje) {
        return ResponseEntity.status(status).body(ApiError.de(status.value(), codigo, mensaje));
    }
}
