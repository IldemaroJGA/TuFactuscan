package com.tufactuscan.documento;

/**
 * Estado de un documento. Coincide con el ENUM de {@code documento.estado}.
 *
 * <ul>
 *   <li>{@link #PENDIENTE}: recién escaneado o subido, falta revisarlo.</li>
 *   <li>{@link #REVISADO}: los datos se han comprobado.</li>
 *   <li>{@link #PAGADO}: la factura ya está pagada (solo lo marca un ADMIN).</li>
 * </ul>
 */
public enum EstadoDocumento {
    PENDIENTE,
    REVISADO,
    PAGADO
}
