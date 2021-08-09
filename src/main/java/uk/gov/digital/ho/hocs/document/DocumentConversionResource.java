package uk.gov.digital.ho.hocs.document;

import com.itextpdf.text.DocumentException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.apache.poi.poifs.filesystem.NotOLE2FileException;
import org.jodconverter.DocumentConverter;
import org.jodconverter.document.DefaultDocumentFormatRegistry;
import org.jodconverter.document.DocumentFormat;
import org.jodconverter.office.OfficeException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import static net.logstash.logback.argument.StructuredArguments.value;
import static uk.gov.digital.ho.hocs.document.LogEvent.*;

@RestController
@Slf4j
public class DocumentConversionResource {

    private final DocumentConverter jodConverter;
    private final ExtendedDocumentConverter extendedDocumentConverter;
    private final MSGDocumentConverter msgDocumentConverter;

    private static final DocumentFormat outputFormat = DefaultDocumentFormatRegistry.PDF;

    @Autowired
    public DocumentConversionResource(DocumentConverter jodConverter, ExtendedDocumentConverter extendedDocumentConverter,
                                      MSGDocumentConverter msgDocumentConverter) {
        this.jodConverter = jodConverter;
        this.extendedDocumentConverter = extendedDocumentConverter;
        this.msgDocumentConverter = msgDocumentConverter;
    }

    // method to convert file
    @PostMapping("/convert")
    public void convert(@RequestParam("file") MultipartFile file, HttpServletResponse response) throws IOException {

        try {
            String fileExtension = FilenameUtils.getExtension(file.getOriginalFilename());
            if (fileExtension == null) {
                log.warn("Failed to convert document {}, file has no extension. Event {}", file.getOriginalFilename(), value(EVENT, DOCUMENT_CONVERSION_INVALID_FORMAT));
                response.setStatus(HttpStatus.BAD_REQUEST.value());
                response.flushBuffer();
                return;
            }

            if (extendedDocumentConverter.isSupported(fileExtension)) {
                InputStream byteStream = new ByteArrayInputStream(file.getBytes());
                try {
                    buildResponse(file, response, byteStream, extendedDocumentConverter.convertToPdf(fileExtension, byteStream));
                    return;
                } catch (DocumentException e) {
                    log.warn("Failed to convert extended document {}, unsupported file format. Event {}. Exception {}", file.getOriginalFilename(), value(EVENT, DOCUMENT_CONVERSION_INVALID_FORMAT), value(EXCEPTION, e.toString()));
                    response.setStatus(HttpStatus.BAD_REQUEST.value());
                    response.flushBuffer();
                    byteStream.close();
                    return;
                }
            }
            if (msgDocumentConverter.isSupported(fileExtension)) {
                InputStream byteStream = new ByteArrayInputStream(file.getBytes());
                try {
                    buildResponse(file, response, byteStream, msgDocumentConverter.convertToPdf(fileExtension, byteStream));
                } catch (DocumentException | NotOLE2FileException e) {
                    log.warn("Failed to convert msg document {}, unsupported file format. Event {}. Exception {}", file.getOriginalFilename(), value(EVENT, DOCUMENT_CONVERSION_INVALID_FORMAT), value(EXCEPTION, e.toString()));
                    response.setStatus(HttpStatus.BAD_REQUEST.value());
                    response.flushBuffer();
                    byteStream.close();
                    return;
                }
            }
            DocumentFormat format = DefaultDocumentFormatRegistry.getFormatByExtension(fileExtension);
            if (format == null) {
                log.warn("Cannot determine document format {}, unsupported file format. Event {}", file.getOriginalFilename(), value(EVENT, DOCUMENT_CONVERSION_INVALID_FORMAT));
                response.setStatus(HttpStatus.BAD_REQUEST.value());
                response.flushBuffer();
                return;
            }

            try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
                jodConverter
                        .convert(file.getInputStream(), true)
                        .as(format)
                        .to(baos)
                        .as(outputFormat)
                        .execute();
    
                setResponseHeaders(file.getOriginalFilename(), response);
                baos.writeTo(response.getOutputStream());
                response.setStatus(HttpStatus.OK.value());
                log.info("Document Conversion complete for {}. Event {}", file.getOriginalFilename(), value(EVENT, DOCUMENT_CONVERSION_SUCCESS));
                response.flushBuffer();
    
            } catch (OfficeException e) {
                log.warn("Failed to convert office document {}. Event {}. Exception {}", file.getOriginalFilename(), value(EVENT, DOCUMENT_CONVERSION_FAILURE), value(EXCEPTION, e.toString()));
                response.setStatus(HttpStatus.BAD_REQUEST.value());
            }
        } catch (Exception e) {
            log.error("Error converting document {}. Event {}. Exception {}", file.getOriginalFilename(), value(EVENT, DOCUMENT_CONVERSION_FAILURE), value(EXCEPTION, e.toString()));
            throw e;
        }

    }

    private void buildResponse(MultipartFile file, HttpServletResponse response, InputStream byteStream, byte[] bytes) throws IOException {
        response.getOutputStream().write(bytes);
        response.setStatus(HttpStatus.OK.value());
        log.info("Document Conversion complete for {}. Event {}", file.getOriginalFilename(), value(EVENT, DOCUMENT_CONVERSION_SUCCESS));
        response.flushBuffer();
        byteStream.close();
    }

    private static void setResponseHeaders(String filename, HttpServletResponse response) {

        response.setContentType(outputFormat.getMediaType());
        response.setHeader(HttpHeaders.CONTENT_DISPOSITION,
                "attachment; filename="
                        + FilenameUtils.getBaseName(filename)
                        + "."
                        + outputFormat.getExtension());
    }
}
