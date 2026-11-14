package com.tufactuscan.proveedor;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

/**
 * Acceso a datos de la tabla {@code proveedor}.
 *
 * <p>Uso: todas las búsquedas reciben el {@code empresaId} para garantizar
 * que cada empresa solo accede a sus propios proveedores.</p>
 */
public interface ProveedorRepository extends JpaRepository<Proveedor, Long> {

    /** Lista los proveedores de una empresa ordenados por nombre. */
    List<Proveedor> findByEmpresaIdOrderByNombreAsc(Long empresaId);

    /**
     * Busca proveedores de la empresa cuyo nombre o NIF contenga el texto
     * (sin distinguir mayúsculas). Consulta JPQL: trabaja con clases, no con tablas.
     */
    @Query("""
            SELECT p FROM Proveedor p
            WHERE p.empresa.id = :empresaId
              AND (LOWER(p.nombre) LIKE LOWER(CONCAT('%', :texto, '%'))
                   OR LOWER(p.nif) LIKE LOWER(CONCAT('%', :texto, '%')))
            ORDER BY p.nombre
            """)
    List<Proveedor> buscar(@Param("empresaId") Long empresaId, @Param("texto") String texto);

    /** Busca un proveedor concreto de la empresa (vacío si no existe o es de otra empresa). */
    Optional<Proveedor> findByIdAndEmpresaId(Long id, Long empresaId);

    /** Busca por NIF dentro de la empresa (usado tras el OCR para autoasignar proveedor). */
    Optional<Proveedor> findByEmpresaIdAndNif(Long empresaId, String nif);
}
