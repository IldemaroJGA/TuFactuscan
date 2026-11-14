package com.tufactuscan;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Punto de entrada del backend de TuFactuScan.
 *
 * <p>Uso: ejecutar este método main desde IntelliJ (botón ▶) o con
 * {@code mvnw spring-boot:run}. Arranca un servidor web en el puerto 8080.</p>
 *
 * <p>Organización modular: cada paquete agrupa todo lo de una parte del
 * negocio (empresa, usuario, proveedor, documento...). Spring encuentra
 * automáticamente las clases de todos los subpaquetes de {@code com.tufactuscan}.</p>
 */
@SpringBootApplication
public class TuFactuScanApplication {

    public static void main(String[] args) {
        SpringApplication.run(TuFactuScanApplication.class, args);
    }
}
