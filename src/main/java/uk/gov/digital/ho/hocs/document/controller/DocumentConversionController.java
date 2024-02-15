package uk.gov.digital.ho.hocs.document.controller;

import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import uk.gov.digital.ho.hocs.document.service.DocumentConversionService;

import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

import static org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE;

@RestController
public class DocumentConversionController {

    private final DocumentConversionService documentConversionService;

    @Autowired
    DocumentConversionController(DocumentConversionService documentConversionService) {
        this.documentConversionService = documentConversionService;
    }

    private static void setResponseHeaders(String filename, HttpServletResponse response) throws IOException {
        response.setHeader(HttpHeaders.CONTENT_DISPOSITION,
                "attachment; filename="
                        + FilenameUtils.getBaseName(filename)
                        + ".pdf");
        response.setStatus(HttpStatus.OK.value());
        response.flushBuffer();
    }

    // method to convert file
    @PostMapping(value = "/convert", consumes = MULTIPART_FORM_DATA_VALUE, produces = {"application/pdf"})
    public void convert(@RequestParam("file") MultipartFile file, HttpServletResponse response) throws IOException {
        documentConversionService.convert(file, response.getOutputStream());
        setResponseHeaders(file.getOriginalFilename(), response);
    }
}
