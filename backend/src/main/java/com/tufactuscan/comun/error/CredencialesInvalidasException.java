package com.tufactuscan.comun.error;

/**
 * Se lanza cuando el login falla (email inexistente, contraseña incorrecta
 * o usuario desactivado).
 *
 * <p>Uso: el mensaje es siempre el mismo para no revelar si el email existe.
 * El manejador global la convierte en un 401.</p>
 */
public class CredencialesInvalidasException extends RuntimeException {

    public CredencialesInvalidasException() {
        super("Email o contraseña incorrectos");
    }
}
