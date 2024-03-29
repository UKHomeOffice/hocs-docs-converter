package uk.gov.digital.ho.hocs.document.service;

import org.apache.pdfbox.pdmodel.PDDocument;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.jodconverter.core.DocumentConverter;
import org.jodconverter.core.document.DefaultDocumentFormatRegistry;
import org.jodconverter.core.document.DocumentFormat;
import org.jodconverter.core.office.OfficeException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import uk.gov.digital.ho.hocs.document.application.exception.ApplicationExceptions;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import static net.logstash.logback.argument.StructuredArguments.value;
import static uk.gov.digital.ho.hocs.document.application.LogEvent.DOCUMENT_CONVERSION_FAILURE;
import static uk.gov.digital.ho.hocs.document.application.LogEvent.DOCUMENT_CONVERSION_INVALID_FORMAT;
import static uk.gov.digital.ho.hocs.document.application.LogEvent.DOCUMENT_CONVERSION_SUCCESS;
import static uk.gov.digital.ho.hocs.document.application.LogEvent.EVENT;

@Slf4j
@Service
public class DocumentConversionService {

    private final DocumentConverter jodConverter;
    private final ImageDocumentConverter imageDocumentConverter;
    private final MsgDocumentConverter msgDocumentConverter;

    @Autowired
    DocumentConversionService(DocumentConverter jodConverter,
                              ImageDocumentConverter imageDocumentConverter,
                              MsgDocumentConverter msgDocumentConverter) {
        this.jodConverter = jodConverter;
        this.imageDocumentConverter = imageDocumentConverter;
        this.msgDocumentConverter = msgDocumentConverter;
    }

    public void convert(MultipartFile file, OutputStream outputStream) {
        String fileExtension = FilenameUtils.getExtension(file.getOriginalFilename());

        if (fileExtension == null) {
            throw new ApplicationExceptions.DocumentFormatException(String.format("Cannot determine document format: %s", file.getOriginalFilename()), DOCUMENT_CONVERSION_INVALID_FORMAT);
        } else {
            fileExtension = fileExtension
                    .trim()
                    .toLowerCase();

            try (InputStream inputStream = file.getInputStream()) {
                DocumentFormat supportedJodFormat = DefaultDocumentFormatRegistry.getFormatByExtension(fileExtension);

                if (fileExtension.equalsIgnoreCase("pdf")){
                    log.info("Document conversion skipped for PDF {}. Event {}",
                            file.getOriginalFilename(), value(EVENT, DOCUMENT_CONVERSION_SUCCESS));
                    IOUtils.copy(inputStream, outputStream);
                    return;
                } else if (imageDocumentConverter.isSupported(fileExtension)) {
                    convertImageToPdf(inputStream, outputStream);
                } else if (msgDocumentConverter.isSupported(fileExtension)) {
                    convertMsgToPdf(inputStream, outputStream);
                } else if (supportedJodFormat != null) {
                    jodConverter
                            .convert(inputStream, false)
                            .as(supportedJodFormat)
                            .to(outputStream)
                            .as(DefaultDocumentFormatRegistry.PDF)
                            .execute();
                } else {
                    throw new ApplicationExceptions.DocumentFormatException(String.format("Cannot convert document type: %s, unsupported file format", file.getOriginalFilename()), DOCUMENT_CONVERSION_INVALID_FORMAT);
                }

            } catch (OfficeException | IOException e) {
                throw new ApplicationExceptions.DocumentConversionException(String.format("Failed to convert document %s for reason %s", file.getOriginalFilename(), e.getMessage()), DOCUMENT_CONVERSION_FAILURE, e);
            }

            log.info("Document Conversion complete for {}. Event {}", file.getOriginalFilename(), value(EVENT, DOCUMENT_CONVERSION_SUCCESS));
        }
    }

    private void convertImageToPdf(InputStream inputStream, OutputStream outputStream) throws IOException {
        PDDocument pdf = new PDDocument();
        imageDocumentConverter.convertToPdf(pdf, inputStream);
        pdf.save(outputStream);
        pdf.close();
    }

    private void convertMsgToPdf(InputStream inputStream, OutputStream outputStream) throws IOException {
        PDDocument pdf = new PDDocument();
        msgDocumentConverter.convertToPdf(pdf, inputStream);
        pdf.save(outputStream);
        pdf.close();
    }
}
