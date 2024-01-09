package uk.gov.digital.ho.hocs.document.service;

import org.jodconverter.core.DocumentConverter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.charset.StandardCharsets;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class DocumentConversionServiceTest {

    @Mock
    private DocumentConverter jodConverter;

    @Mock
    private ImageDocumentConverter imageDocumentConverter;

    @Mock
    private MsgDocumentConverter msgDocumentConverter;

    private DocumentConversionService documentConversionService;

    @BeforeEach
    public void setup() {
        documentConversionService =
                new DocumentConversionService(jodConverter, imageDocumentConverter, msgDocumentConverter);
    }

    @Test
    public void shouldPassThroughPdfUnchanged() throws IOException {
        MultipartFile multipartFile = generateMultipartFile("testFile.pdf", "testFile.pdf");
        ByteArrayOutputStream result = new ByteArrayOutputStream();

        documentConversionService.convert(multipartFile, result);

        assert (result.toString(StandardCharsets.UTF_8)).equals("TEST");

        verifyNoInteractions(msgDocumentConverter);
        verifyNoInteractions(imageDocumentConverter);
        verifyNoInteractions(jodConverter);
    }

    private MultipartFile generateMultipartFile(String name, String originalFilename) {
        return new MultipartFile() {

            @Override
            public String getName() {
                return name;
            }

            @Override
            public String getOriginalFilename() {
                return originalFilename;
            }

            @Override
            public String getContentType() {
                return null;
            }

            @Override
            public boolean isEmpty() {
                return false;
            }

            @Override
            public long getSize() {
                return 0;
            }

            @Override
            public byte[] getBytes() throws IOException {
                return "TEST".getBytes(StandardCharsets.UTF_8);
            }

            @Override
            public InputStream getInputStream() throws IOException {
                return new ByteArrayInputStream("TEST".getBytes(StandardCharsets.UTF_8));
            }

            @Override
            public void transferTo(File dest) throws IOException, IllegalStateException {

            }
        };
    }
}
