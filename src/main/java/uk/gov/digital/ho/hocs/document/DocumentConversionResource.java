package uk.gov.digital.ho.hocs.document;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;

import java.io.IOException;

@RestController
@Slf4j
public class DocumentConversionResource {

    @Autowired
    DocumentConversionResource(DocumentConversionService documentConversionService) {
        this.documentConversionService = documentConversionService;
    }

    private DocumentConversionService documentConversionService;

    // method to convert file
    @PostMapping("/convert")
    public void convert(@RequestParam("file") MultipartFile file, HttpServletResponse response) throws IOException {
        setResponseHeaders(file.getOriginalFilename(), response);
        documentConversionService.convert(file, response);
    }

    private static void setResponseHeaders(String filename, HttpServletResponse response) {
        response.setContentType(MediaType.APPLICATION_PDF.toString());
        response.setHeader(HttpHeaders.CONTENT_DISPOSITION,
                "attachment; filename="
                        + FilenameUtils.getBaseName(filename)
                        + ".pdf");
    }
}
