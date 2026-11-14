package com.tufactuscan.documento;

import jakarta.persistence.*;

import java.math.BigDecimal;

/**
 * Entidad LINEA_DOCUMENTO: un concepto dentro de una factura o albarán
 * (por ejemplo, "Filtro de aceite x10").
 *
 * <p>Uso: es opcional; se rellena si el OCR detecta las líneas o si el
 * usuario las introduce. Se guarda y se borra siempre junto a su documento.</p>
 *
 * <p>Tabla: {@code linea_documento}.</p>
 */
@Entity
@Table(name = "linea_documento")
public class LineaDocumento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Documento al que pertenece la línea. */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "documento_id", nullable = false)
    private Documento documento;

    /** Posición de la línea dentro del documento (1, 2, 3...). */
    @Column(nullable = false)
    private Short orden = 1;

    @Column(nullable = false, length = 255)
    private String descripcion;

    @Column(nullable = false, precision = 10, scale = 3)
    private BigDecimal cantidad = BigDecimal.ONE;

    @Column(name = "precio_unitario", precision = 12, scale = 4)
    private BigDecimal precioUnitario;

    /** Porcentaje de IVA: 21.00, 10.00, 4.00 o 0.00. */
    @Column(name = "iva_porcentaje", precision = 5, scale = 2)
    private BigDecimal ivaPorcentaje;

    @Column(precision = 12, scale = 2)
    private BigDecimal importe;

    /** Constructor vacío obligatorio para JPA. */
    protected LineaDocumento() {
    }

    /** Crea una línea con su descripción y cantidad. */
    public LineaDocumento(short orden, String descripcion, BigDecimal cantidad) {
        this.orden = orden;
        this.descripcion = descripcion;
        this.cantidad = cantidad;
    }

    // ----- Getters y setters -----

    public Long getId() { return id; }

    public Documento getDocumento() { return documento; }
    /** Lo usa {@link Documento#addLinea}; no llamar directamente. */
    void setDocumento(Documento documento) { this.documento = documento; }

    public Short getOrden() { return orden; }
    public void setOrden(Short orden) { this.orden = orden; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public BigDecimal getCantidad() { return cantidad; }
    public void setCantidad(BigDecimal cantidad) { this.cantidad = cantidad; }

    public BigDecimal getPrecioUnitario() { return precioUnitario; }
    public void setPrecioUnitario(BigDecimal precioUnitario) { this.precioUnitario = precioUnitario; }

    public BigDecimal getIvaPorcentaje() { return ivaPorcentaje; }
    public void setIvaPorcentaje(BigDecimal ivaPorcentaje) { this.ivaPorcentaje = ivaPorcentaje; }

    public BigDecimal getImporte() { return importe; }
    public void setImporte(BigDecimal importe) { this.importe = importe; }
}
