package com.copypoint.api.domain.attachment.service;


import com.copypoint.api.domain.attachment.Attachment;
import com.copypoint.api.domain.attachment.AttachmentFileType;
import com.copypoint.api.infra.cloudflare.r2.service.CloudflareR2Service;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.poi.hwpf.HWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.IOException;

@Slf4j
@Service
public class PageCountService {
    @Autowired
    private CloudflareR2Service cloudflareR2Service;

    /**
     * Calcula el número de páginas de un attachment
     */
    public int calculatePages(Attachment attachment) throws IOException {
        log.debug("Calculando páginas para attachment ID: {}, tipo: {}",
                attachment.getId(), attachment.getFileType());

        if (attachment.getStoragePath() == null || !attachment.isDownloaded()) {
            throw new IllegalStateException("El attachment no está descargado o no tiene ruta de almacenamiento");
        }

        // Descargar el archivo desde R2
        byte[] fileContent = cloudflareR2Service.downloadFile(attachment.getStoragePath());

        return switch (attachment.getFileType()) {
            case PDF -> calculatePdfPages(fileContent);
            case DOC -> calculateDocPages(fileContent);
            case DOCX -> calculateDocxPages(fileContent);
            case IMAGE, PNG, JPG -> 1; // Las imágenes siempre son 1 página
            default -> {
                log.warn("Tipo de archivo no soportado para cálculo de páginas: {}", attachment.getFileType());
                yield 1; // Por defecto 1 página
            }
        };
    }

    /**
     * Calcula páginas de un archivo PDF
     */
    private int calculatePdfPages(byte[] fileContent) throws IOException {
        try (PDDocument document = Loader.loadPDF(fileContent)) {
            int pageCount = document.getNumberOfPages();
            log.debug("PDF tiene {} páginas", pageCount);
            return pageCount;
        } catch (Exception e) {
            log.error("Error leyendo PDF: {}", e.getMessage(), e);
            throw new IOException("Error procesando archivo PDF: " + e.getMessage(), e);
        }
    }

    /**
     * Calcula páginas de un archivo DOC (formato antiguo de Word)
     */
    private int calculateDocPages(byte[] fileContent) throws IOException {
        try (HWPFDocument document = new HWPFDocument(new ByteArrayInputStream(fileContent))) {
            // Para archivos DOC es más complejo calcular páginas exactas
            // Una aproximación es contar la cantidad de texto y estimar
            String text = document.getDocumentText();

            // Estimación: aproximadamente 500 palabras por página
            int wordCount = text.split("\\s+").length;
            int estimatedPages = Math.max(1, (int) Math.ceil(wordCount / 500.0));

            log.debug("DOC estimado en {} páginas (basado en {} palabras)", estimatedPages, wordCount);
            return estimatedPages;
        } catch (Exception e) {
            log.error("Error leyendo archivo DOC: {}", e.getMessage(), e);
            throw new IOException("Error procesando archivo DOC: " + e.getMessage(), e);
        }
    }

    /**
     * Calcula páginas de un archivo DOCX (formato nuevo de Word)
     */
    private int calculateDocxPages(byte[] fileContent) throws IOException {
        try (XWPFDocument document = new XWPFDocument(new ByteArrayInputStream(fileContent))) {
            // Para DOCX también es complejo calcular páginas exactas
            // Usamos una estimación basada en párrafos y saltos de página

            int paragraphCount = document.getParagraphs().size();

            // Contar saltos de página explícitos
            int pageBreaks = 0;
            var paragraphs = document.getParagraphs();
            for (var paragraph : paragraphs) {
                String text = paragraph.getText();
                if (text.contains("\f") || text.contains("\u000C")) { // Form feed character
                    pageBreaks++;
                }
            }

            // Estimación: si hay saltos de página explícitos, usar esos + 1
            // Si no, estimar basado en párrafos (aprox 25 párrafos por página)
            int estimatedPages;
            if (pageBreaks > 0) {
                estimatedPages = pageBreaks + 1;
            } else {
                estimatedPages = Math.max(1, (int) Math.ceil(paragraphCount / 25.0));
            }

            log.debug("DOCX estimado en {} páginas (basado en {} párrafos y {} saltos)",
                    estimatedPages, paragraphCount, pageBreaks);
            return estimatedPages;
        } catch (Exception e) {
            log.error("Error leyendo archivo DOCX: {}", e.getMessage(), e);
            throw new IOException("Error procesando archivo DOCX: " + e.getMessage(), e);
        }
    }

    /**
     * Verifica si un tipo de archivo es soportado para cálculo de páginas
     */
    public boolean isFileTypeSupported(AttachmentFileType fileType) {
        return switch (fileType) {
            case PDF, DOC, DOCX, IMAGE, PNG, JPG -> true;
            default -> false;
        };
    }

    /**
     * Obtiene el número de páginas por defecto para un tipo de archivo no soportado
     */
    public int getDefaultPagesForFileType(AttachmentFileType fileType) {
        return switch (fileType) {
            case IMAGE, PNG, JPG -> 1;
            case PDF, DOC, DOCX, DOCUMENT -> 1;
            case PRESENTATION -> 10; // Estimación para presentaciones
            case SPREADSHEET -> 1;   // Las hojas de cálculo se consideran 1 página
            default -> 1;
        };
    }
}
