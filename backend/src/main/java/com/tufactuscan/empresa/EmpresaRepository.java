package com.tufactuscan.empresa;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Acceso a datos de la tabla {@code empresa}.
 *
 * <p>Uso: Spring Data genera la implementación automáticamente. Hereda de
 * {@link JpaRepository} los métodos básicos: findAll, findById, save,
 * deleteById, count...</p>
 */
public interface EmpresaRepository extends JpaRepository<Empresa, Long> {

    /** Busca una empresa por su NIF (Spring crea la consulta a partir del nombre del método). */
    Optional<Empresa> findByNif(String nif);
}
