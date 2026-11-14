package com.tufactuscan.documento;

import jakarta.persistence.*;

import java.time.LocalDateTime;

/**
 * Entidad IMAGEN_DOCUMENTO: fichero asociado a un documento (una página
 * escaneada en JPEG/PNG o un PDF completo).
 *
 * <p>Uso: el fichero se guarda en disco (carpeta de subidas) y en la base de
 * datos solo su ruta y metadatos, para no llenar MySQL de datos binarios.</p>
 *
 * <p>Tabla: {@code imagen_documento}.</p>
 */
@Entity
@Table(name = "imagen_documento")
public class ImagenDocumento {

    /** Tipo MIME de los PDF; se usa para distinguirlos de las imágenes. */
    public static final String MIME_PDF = "application/pdf";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "documento_id", nullable = false)
    private Documento documento;

    /** Número de página (1, 2...). Un PDF de varias páginas ocupa una sola fila. */
    @Column(nullable = false)
    private Short pagina = 1;

    /** Ruta relativa dentro de la carpeta de subidas, p. ej. "empresa-1/2026/11/uuid-p1.jpg". */
    @Column(name = "ruta_fichero", nullable = false, length = 255)
    private String rutaFichero;

    /** image/jpeg, image/png o application/pdf. */
    @Column(name = "tipo_mime", nullable = false, length = 50)
    private String tipoMime;

    @Column(name = "tamano_bytes", nullable = false)
    private Long tamanoBytes;

    @Column(name = "creado_en", nullable = false, updatable = false)
    private LocalDateTime creadoEn;

    /** Constructor vacío obligatorio para JPA. */
    protected ImagenDocumento() {
    }

    /** Crea el registro de un fichero ya guardado en disco. */
    public ImagenDocumento(short pagina, String rutaFichero, String tipoMime, long tamanoBytes) {
        this.pagina = pagina;
        this.rutaFichero = rutaFichero;
        this.tipoMime = tipoMime;
        this.tamanoBytes = tamanoBytes;
    }

    @PrePersist
    void alCrear() {
        this.creadoEn = LocalDateTime.now();
    }

    /** Indica si el fichero es un PDF. */
    public boolean esPdf() {
        return MIME_PDF.equals(tipoMime);
    }

    // ----- Getters y setters -----

    public Long getId() { return id; }

    public Documento getDocumento() { return documento; }
    /** Lo usa {@link Documento#addImagen}; no llamar directamente. */
    void setDocumento(Documento documento) { this.documento = documento; }

    public Short getPagina() { return pagina; }

    public String getRutaFichero() { return rutaFichero; }

    public String getTipoMime() { return tipoMime; }

    public Long getTamanoBytes() { return tamanoBytes; }

    public LocalDateTime getCreadoEn() { return creadoEn; }
}
