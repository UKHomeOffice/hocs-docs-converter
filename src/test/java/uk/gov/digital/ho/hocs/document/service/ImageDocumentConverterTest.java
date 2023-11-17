package uk.gov.digital.ho.hocs.document.service;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest(webEnvironment = RANDOM_PORT)
public class ImageDocumentConverterTest {

    private final ImageDocumentConverter imageDocumentConverter = new ImageDocumentConverter();

    private static Stream<Arguments> getFiles() {
        return Stream.of(
                Arguments.of("src/test/resources/testdata/sample.gif", "gif", 35568),
                Arguments.of("src/test/resources/testdata/sample.jpg", "jpg", 447932),
                Arguments.of("src/test/resources/testdata/sample.png", "png", 183127),
                Arguments.of("src/test/resources/testdata/sample.tif", "tif", 2511535)
        );
    }

    @ParameterizedTest
    @MethodSource("getFiles")
    public void textExtendedDocumentConverterTiffDirectly(String resourcePath, String extension, int size) throws IOException {

        Resource resource = new FileSystemResource(resourcePath);
        FileInputStream inputStream = new FileInputStream(resource.getFile());

        PDDocument pdf = new PDDocument();
        try (ByteArrayOutputStream arrayOutputStream = new ByteArrayOutputStream()) {

            imageDocumentConverter.convertToPdf(pdf, inputStream);

            inputStream.close();
            assertEquals(1, pdf.getNumberOfPages());

            //Write the file to the project root, so we can inspect it if we want
            FileOutputStream fos = new FileOutputStream("sample." + extension + ".pdf");
            pdf.save(arrayOutputStream);
            fos.write(arrayOutputStream.toByteArray());
            fos.flush();
            fos.close();
            pdf.close();
        }
    }

    @Test
    public void checkSupport() {
        assertTrue(imageDocumentConverter.isSupported("jpg"));
        assertTrue(imageDocumentConverter.isSupported("png"));
        assertTrue(imageDocumentConverter.isSupported("tif"));
        assertTrue(imageDocumentConverter.isSupported("gif"));
        assertTrue(imageDocumentConverter.isSupported("jpeg"));
        assertTrue(imageDocumentConverter.isSupported("tiff"));

        assertFalse(imageDocumentConverter.isSupported("doc"));
        assertFalse(imageDocumentConverter.isSupported("rtf"));
        assertFalse(imageDocumentConverter.isSupported("txt"));
        assertFalse(imageDocumentConverter.isSupported("docx"));
        assertFalse(imageDocumentConverter.isSupported("msg"));
        assertFalse(imageDocumentConverter.isSupported("pdf"));
    }

}
