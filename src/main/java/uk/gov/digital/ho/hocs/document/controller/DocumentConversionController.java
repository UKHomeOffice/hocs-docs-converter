package uk.gov.digital.ho.hocs.document.controller;

import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import uk.gov.digital.ho.hocs.document.service.DocumentConversionService;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@RestController
public class DocumentConversionController {

    private final DocumentConversionService documentConversionService;

    @Autowired
    DocumentConversionController(DocumentConversionService documentConversionService) {
        this.documentConversionService = documentConversionService;
    }

    private static void setResponseHeaders(String filename, HttpServletResponse response) throws IOException {
        response.setContentType(MediaType.APPLICATION_PDF.toString());
        response.setHeader(HttpHeaders.CONTENT_DISPOSITION,
                "attachment; filename="
                        + FilenameUtils.getBaseName(filename)
                        + ".pdf");
        response.setStatus(HttpStatus.OK.value());
        response.flushBuffer();
    }

    // method to convert file
    @PostMapping("/convert")
    public void convert(@RequestParam("file") MultipartFile file, HttpServletResponse response) throws IOException {
        documentConversionService.convert(file, response);
        setResponseHeaders(file.getOriginalFilename(), response);
    }
}
