package uk.gov.digital.ho.hocs.document;

import com.itextpdf.text.Document;
import com.itextpdf.text.pdf.PdfWriter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.jodconverter.core.DocumentConverter;
import org.jodconverter.core.document.DefaultDocumentFormatRegistry;
import org.jodconverter.core.document.DocumentFormat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import static net.logstash.logback.argument.StructuredArguments.value;
import static uk.gov.digital.ho.hocs.document.LogEvent.DOCUMENT_CONVERSION_FAILURE;
import static uk.gov.digital.ho.hocs.document.LogEvent.DOCUMENT_CONVERSION_INVALID_FORMAT;
import static uk.gov.digital.ho.hocs.document.LogEvent.DOCUMENT_CONVERSION_SUCCESS;
import static uk.gov.digital.ho.hocs.document.LogEvent.EVENT;
import static uk.gov.digital.ho.hocs.document.LogEvent.EXCEPTION;

@Slf4j
@Service
public class DocumentConversionService {

    @Autowired
    DocumentConversionService(
            DocumentConverter jodConverter,
            ImageDocumentConverter imageDocumentConverter,
            MsgDocumentConverter msgDocumentConverter) {
        this.jodConverter = jodConverter;
        this.imageDocumentConverter = imageDocumentConverter;
        this.msgDocumentConverter = msgDocumentConverter;
    }

    private final DocumentConverter jodConverter;
    private final ImageDocumentConverter imageDocumentConverter;
    private final MsgDocumentConverter msgDocumentConverter;

    public void convert(MultipartFile file, HttpServletResponse response) throws IOException {
        String fileExtension = FilenameUtils.getExtension(file.getOriginalFilename());

        if (fileExtension == null) {
            log.warn("Failed to convert document {}, file has no extension. Event {}", file.getOriginalFilename(), value(EVENT, DOCUMENT_CONVERSION_INVALID_FORMAT));
            response.setStatus(HttpStatus.BAD_REQUEST.value());
        } else {

            try (InputStream inputStream = new ByteArrayInputStream(file.getBytes());
                 ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {

                boolean extendedSupport = imageDocumentConverter.isSupported(fileExtension);
                boolean msgSupport = msgDocumentConverter.isSupported(fileExtension);
                DocumentFormat jodSupportedFormat = DefaultDocumentFormatRegistry.getFormatByExtension(fileExtension);

                if(extendedSupport || msgSupport) {
                    Document pdf = new Document();
                    PdfWriter.getInstance(pdf, outputStream);
                    pdf.open();

                    if (extendedSupport) {
                        imageDocumentConverter.convertToPdf(pdf, fileExtension, inputStream);
                    } else {
                        msgDocumentConverter.convertToPdf(pdf, fileExtension, inputStream);
                    }

                    pdf.close();

                    response.setStatus(HttpStatus.OK.value());
                } else if (jodSupportedFormat != null) {

                    jodConverter
                            .convert(inputStream, false)
                            .as(jodSupportedFormat)
                            .to(outputStream)
                            .as(DefaultDocumentFormatRegistry.PDF)
                            .execute();

                    response.setStatus(HttpStatus.OK.value());
                } else {
                    log.warn("Cannot determine document format {}, unsupported file format. Event {}", file.getOriginalFilename(), value(EVENT, DOCUMENT_CONVERSION_INVALID_FORMAT));
                    response.setStatus(HttpStatus.BAD_REQUEST.value());
                }

                response.getOutputStream().write(outputStream.toByteArray());

            } catch (Exception e) {
                log.warn("Failed to convert document {}, unsupported file format. Event {}. Exception {}", file.getOriginalFilename(), value(EVENT, DOCUMENT_CONVERSION_FAILURE), value(EXCEPTION, e.toString()));
                response.setStatus(HttpStatus.BAD_REQUEST.value());
            }

            response.flushBuffer();
            log.info("Document Conversion complete for {}. Event {}", file.getOriginalFilename(), value(EVENT, DOCUMENT_CONVERSION_SUCCESS));
        }
    }
}
