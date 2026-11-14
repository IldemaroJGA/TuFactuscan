package com.tufactuscan.comun.error;

/**
 * Se lanza cuando la operación choca con datos existentes o con una regla
 * de negocio (NIF duplicado, vincular una factura a otra factura...).
 *
 * <p>Uso: {@code throw new ConflictoException("Ya existe un proveedor con ese NIF");}
 * El manejador global la convierte en un 409.</p>
 */
public class ConflictoException extends RuntimeException {

    public ConflictoException(String mensaje) {
        super(mensaje);
    }
}
