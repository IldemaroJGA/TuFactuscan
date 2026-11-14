package com.tufactuscan.usuario;

/**
 * Roles de usuario. Coinciden con el ENUM de la columna {@code usuario.rol}.
 *
 * <ul>
 *   <li>{@link #ADMIN}: gestiona la empresa, los usuarios y todos los documentos.</li>
 *   <li>{@link #EMPLEADO}: escanea, sube PDF y consulta documentos.</li>
 * </ul>
 */
public enum Rol {
    ADMIN,
    EMPLEADO
}
