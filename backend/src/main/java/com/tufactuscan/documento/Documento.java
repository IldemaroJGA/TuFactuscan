package com.tufactuscan.documento;

import com.tufactuscan.empresa.Empresa;
import com.tufactuscan.proveedor.Proveedor;
import com.tufactuscan.usuario.Usuario;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Entidad DOCUMENTO: una factura o un albarán escaneado o subido en PDF.
 *
 * <p>Uso: es el centro del sistema. Guarda los datos que extrae el OCR
 * (y que el usuario revisa), su estado y, si es un albarán, la factura a la
 * que está vinculado.</p>
 *
 * <p>Sincronización: el {@code uuid} lo genera el móvil, así un mismo
 * documento enviado dos veces no se duplica. El borrado es lógico
 * ({@code eliminado = true}) para que el resto de dispositivos se enteren.</p>
 *
 * <p>Importes en {@link BigDecimal}: nunca usar double para dinero.</p>
 *
 * <p>Tabla: {@code documento}.</p>
 */
@Entity
@Table(name = "documento")
public class Documento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Identificador generado en el dispositivo (CHAR(36)). */
    @Column(nullable = false, unique = true, length = 36, columnDefinition = "CHAR(36)")
    private String uuid;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "empresa_id", nullable = false)
    private Empresa empresa;

    /** Proveedor emisor; puede faltar hasta que se revise. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "proveedor_id")
    private Proveedor proveedor;

    /** Usuario que lo escaneó o subió. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;

    /** Solo para albaranes: factura que los agrupa (relación de la tabla consigo misma). */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "factura_id")
    private Documento factura;

    /** Albaranes vinculados a esta factura (lado inverso de {@code factura}). */
    @OneToMany(mappedBy = "factura")
    private List<Documento> albaranes = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoDocumento tipo;

    @Column(length = 50)
    private String numero;

    @Column(name = "fecha_emision")
    private LocalDate fechaEmision;

    @Column(name = "base_imponible", precision = 12, scale = 2)
    private BigDecimal baseImponible;

    @Column(name = "cuota_iva", precision = 12, scale = 2)
    private BigDecimal cuotaIva;

    @Column(precision = 12, scale = 2)
    private BigDecimal total;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoDocumento estado = EstadoDocumento.PENDIENTE;

    /** Texto bruto reconocido por el OCR (se guarda para poder revisarlo). */
    @Column(name = "texto_ocr", columnDefinition = "TEXT")
    private String textoOcr;

    @Column(length = 500)
    private String observaciones;

    @Column(name = "creado_en", nullable = false, updatable = false)
    private LocalDateTime creadoEn;

    @Column(name = "actualizado_en", nullable = false)
    private LocalDateTime actualizadoEn;

    /** Borrado lógico: true = eliminado, pero se conserva para sincronizar. */
    @Column(nullable = false)
    private boolean eliminado = false;

    /** Líneas de detalle; se guardan y borran junto con el documento. */
    @OneToMany(mappedBy = "documento", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("orden ASC")
    private List<LineaDocumento> lineas = new ArrayList<>();

    /** Imágenes o PDF del documento; se guardan y borran junto con él. */
    @OneToMany(mappedBy = "documento", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("pagina ASC")
    private List<ImagenDocumento> imagenes = new ArrayList<>();

    /** Constructor vacío obligatorio para JPA. */
    protected Documento() {
    }

    /** Crea un documento nuevo en estado PENDIENTE. */
    public Documento(String uuid, Empresa empresa, TipoDocumento tipo) {
        this.uuid = uuid;
        this.empresa = empresa;
        this.tipo = tipo;
    }

    /** Fija las fechas al insertar. */
    @PrePersist
    void alCrear() {
        LocalDateTime ahora = LocalDateTime.now();
        this.creadoEn = ahora;
        this.actualizadoEn = ahora;
    }

    /** Actualiza la fecha de modificación (la usa la sincronización). */
    @PreUpdate
    void alActualizar() {
        this.actualizadoEn = LocalDateTime.now();
    }

    // ----- Métodos de negocio -----

    /** Indica si es una factura. */
    public boolean esFactura() {
        return tipo == TipoDocumento.FACTURA;
    }

    /** Indica si es un albarán. */
    public boolean esAlbaran() {
        return tipo == TipoDocumento.ALBARAN;
    }

    /** Añade una línea manteniendo la relación en los dos sentidos. */
    public void addLinea(LineaDocumento linea) {
        linea.setDocumento(this);
        lineas.add(linea);
    }

    /** Añade una imagen/PDF manteniendo la relación en los dos sentidos. */
    public void addImagen(ImagenDocumento imagen) {
        imagen.setDocumento(this);
        imagenes.add(imagen);
    }

    // ----- Getters y setters -----

    public Long getId() { return id; }

    public String getUuid() { return uuid; }

    public Empresa getEmpresa() { return empresa; }

    public Proveedor getProveedor() { return proveedor; }
    public void setProveedor(Proveedor proveedor) { this.proveedor = proveedor; }

    public Usuario getUsuario() { return usuario; }
    public void setUsuario(Usuario usuario) { this.usuario = usuario; }

    public Documento getFactura() { return factura; }
    public void setFactura(Documento factura) { this.factura = factura; }

    public List<Documento> getAlbaranes() { return albaranes; }

    public TipoDocumento getTipo() { return tipo; }
    public void setTipo(TipoDocumento tipo) { this.tipo = tipo; }

    public String getNumero() { return numero; }
    public void setNumero(String numero) { this.numero = numero; }

    public LocalDate getFechaEmision() { return fechaEmision; }
    public void setFechaEmision(LocalDate fechaEmision) { this.fechaEmision = fechaEmision; }

    public BigDecimal getBaseImponible() { return baseImponible; }
    public void setBaseImponible(BigDecimal baseImponible) { this.baseImponible = baseImponible; }

    public BigDecimal getCuotaIva() { return cuotaIva; }
    public void setCuotaIva(BigDecimal cuotaIva) { this.cuotaIva = cuotaIva; }

    public BigDecimal getTotal() { return total; }
    public void setTotal(BigDecimal total) { this.total = total; }

    public EstadoDocumento getEstado() { return estado; }
    public void setEstado(EstadoDocumento estado) { this.estado = estado; }

    public String getTextoOcr() { return textoOcr; }
    public void setTextoOcr(String textoOcr) { this.textoOcr = textoOcr; }

    public String getObservaciones() { return observaciones; }
    public void setObservaciones(String observaciones) { this.observaciones = observaciones; }

    public LocalDateTime getCreadoEn() { return creadoEn; }

    public LocalDateTime getActualizadoEn() { return actualizadoEn; }

    public boolean isEliminado() { return eliminado; }
    public void setEliminado(boolean eliminado) { this.eliminado = eliminado; }

    public List<LineaDocumento> getLineas() { return lineas; }

    public List<ImagenDocumento> getImagenes() { return imagenes; }
}
