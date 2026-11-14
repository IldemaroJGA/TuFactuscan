package com.tufactuscan.comun.error;

/**
 * Se lanza cuando un recurso no existe o pertenece a otra empresa.
 *
 * <p>Uso: {@code throw new NoEncontradoException("Documento no encontrado");}
 * El manejador global la convierte en un 404. Se usa el mismo error en los
 * dos casos para no revelar que existe un dato de otra empresa.</p>
 */
public class NoEncontradoException extends RuntimeException {

    public NoEncontradoException(String mensaje) {
        super(mensaje);
    }
}
