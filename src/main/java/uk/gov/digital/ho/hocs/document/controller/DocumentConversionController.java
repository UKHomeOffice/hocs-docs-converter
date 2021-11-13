package uk.gov.digital.ho.hocs.document.controller;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import uk.gov.digital.ho.hocs.document.service.DocumentConversionService;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@RestController
@Slf4j
public class DocumentConversionController {

    private final DocumentConversionService documentConversionService;

    @Autowired
    DocumentConversionController(DocumentConversionService documentConversionService) {
        this.documentConversionService = documentConversionService;
    }

    private static void setResponseHeaders(String filename, HttpServletResponse response) {
        response.setContentType(MediaType.APPLICATION_PDF.toString());
        response.setHeader(HttpHeaders.CONTENT_DISPOSITION,
                "attachment; filename="
                        + FilenameUtils.getBaseName(filename)
                        + ".pdf");
    }

    // method to convert file
    @PostMapping("/convert")
    public void convert(@RequestParam("file") MultipartFile file, HttpServletResponse response) throws IOException {
        setResponseHeaders(file.getOriginalFilename(), response);
        documentConversionService.convert(file, response);
    }
}
