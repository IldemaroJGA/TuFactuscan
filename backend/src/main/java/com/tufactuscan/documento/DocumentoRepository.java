package com.tufactuscan.documento;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Acceso a datos de la tabla {@code documento}.
 *
 * <p>Uso: todas las consultas filtran por empresa. Los filtros avanzados del
 * listado (proveedor, fechas, importes...) se añadirán en el bloque 3.</p>
 */
public interface DocumentoRepository extends JpaRepository<Documento, Long> {

    /** Documento concreto de la empresa que no esté eliminado. */
    Optional<Documento> findByIdAndEmpresaIdAndEliminadoFalse(Long id, Long empresaId);

    /** Busca por uuid (lo usa la sincronización para no duplicar). */
    Optional<Documento> findByUuid(String uuid);

    /** Documentos de la empresa no eliminados, del más reciente al más antiguo. */
    List<Documento> findByEmpresaIdAndEliminadoFalseOrderByFechaEmisionDesc(Long empresaId);

    /** Cambios desde una fecha, incluidos los eliminados (sincronización con el móvil). */
    List<Documento> findByEmpresaIdAndActualizadoEnAfter(Long empresaId, LocalDateTime desde);

    /** Cuenta los documentos activos de una empresa. */
    long countByEmpresaIdAndEliminadoFalse(Long empresaId);
}
