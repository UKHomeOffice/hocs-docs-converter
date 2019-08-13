package uk.gov.digital.ho.hocs.document;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
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
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import static net.logstash.logback.argument.StructuredArguments.value;
import static uk.gov.digital.ho.hocs.document.LogEvent.*;

@RestController
@Slf4j
public class DocumentConversionResource {

    private final DocumentConverter converter;
    private static final DocumentFormat outputFormat = DefaultDocumentFormatRegistry.PDF;

    @Autowired
    public DocumentConversionResource(DocumentConverter converter) {
        this.converter = converter;
    }


    // method to convert file
    @PostMapping("/convert")
    public void convert(@RequestParam("file") MultipartFile file, HttpServletResponse response) throws IOException {

        String fileExtension = FilenameUtils.getExtension(file.getOriginalFilename());
        if (fileExtension == null) {
            log.info("Cannot convert document {}, file has no extension", file.getOriginalFilename(), value(EVENT, DOCUMENT_CONVERSION_INVALID_FORMAT));
            response.setStatus(HttpStatus.BAD_REQUEST.value());
            response.flushBuffer();
            return;
        }

        DocumentFormat format = DefaultDocumentFormatRegistry.getFormatByExtension(fileExtension);
        if (format == null) {
            log.info("Cannot convert document {}, unsupported file format", file.getOriginalFilename(), value(EVENT, DOCUMENT_CONVERSION_INVALID_FORMAT));
            response.setStatus(HttpStatus.BAD_REQUEST.value());
            response.flushBuffer();
            return;
        }

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            converter
                    .convert(file.getInputStream(), true)
                    .as(format)
                    .to(baos)
                    .as(outputFormat)
                    .execute();

            setResponseHeaders(file.getOriginalFilename(), response);
            baos.writeTo(response.getOutputStream());
            response.setStatus(HttpStatus.OK.value());
            log.info("Document Conversion complete for {}", file.getOriginalFilename(), value(EVENT, DOCUMENT_CONVERSION_SUCCESS));
            response.flushBuffer();

        } catch (OfficeException | IOException e) {
            log.error("Error converting document {}", e.getMessage(), value(EVENT, DOCUMENT_CONVERSION_FAILURE), value(EXCEPTION, e.toString()));
            response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
        }

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
