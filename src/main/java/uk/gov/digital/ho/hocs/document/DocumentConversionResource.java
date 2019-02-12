package uk.gov.digital.ho.hocs.document;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.jodconverter.DocumentConverter;
import org.jodconverter.document.DefaultDocumentFormatRegistry;
import org.jodconverter.document.DocumentFormat;
import org.jodconverter.office.OfficeException;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import static net.logstash.logback.argument.StructuredArguments.value;
import static uk.gov.digital.ho.hocs.document.LogEvent.*;

@RestController
@Slf4j
public class DocumentConversionResource {

    private DocumentConverter converter;
    private final DocumentFormat outputFormat =  DefaultDocumentFormatRegistry.PDF;

    @Autowired
    public DocumentConversionResource( DocumentConverter converter) {
        this.converter  = converter;
    }

    @PostMapping("/uploadFile")
    public void uploadFile(@RequestParam("file") MultipartFile file, HttpServletResponse response)  {


        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            converter
                    .convert(file.getInputStream())
                    .as(
                        DefaultDocumentFormatRegistry.getFormatByExtension(
                            FilenameUtils.getExtension(file.getOriginalFilename())))
                    .to(baos)
                    .as(outputFormat)
                    .execute();

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType(outputFormat.getMediaType()));
            headers.add(HttpHeaders.CONTENT_DISPOSITION,
                "attachment; filename="
                        + FilenameUtils.getBaseName(file.getOriginalFilename())
                        + "."
                        + outputFormat.getExtension());
            baos.writeTo(response.getOutputStream());
            response.setStatus(HttpStatus.OK.value());
            log.info("Document Conversion complete for {}", file.getOriginalFilename(), value(EVENT, DOCUMENT_CONVERSION_SUCCESS));
            response.flushBuffer();

    } catch (OfficeException | IOException e) {
            log.error("Error converting document {}", e.getMessage(), value(EVENT, DOCUMENT_CONVERSION_FAILURE));
            response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
    }

    }

}
