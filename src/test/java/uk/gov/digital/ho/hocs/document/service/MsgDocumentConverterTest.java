package uk.gov.digital.ho.hocs.document.service;


import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.pdf.PdfWriter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import uk.gov.digital.ho.hocs.document.domain.MsgContents;

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
public class MsgDocumentConverterTest {

    private MsgDocumentConverter msgDocumentConverter;

    @Autowired
    public ImageDocumentConverter imageDocumentConverter;

    @BeforeEach
    public void setup() {
        msgDocumentConverter =
                new MsgDocumentConverter(imageDocumentConverter);
    }

    private static Stream<Arguments> getFiles() {
        return Stream.of(
                Arguments.of("src/test/resources/testdata/sample1.msg", 23660),
                Arguments.of("src/test/resources/testdata/sample2.msg", 23660),
                Arguments.of("src/test/resources/testdata/sample3.msg", 23659),
                Arguments.of("src/test/resources/testdata/sample4.MSG", 985)
        );
    }

    @ParameterizedTest
    @MethodSource("getFiles")
    public void textExtendedDocumentConverterTiffDirectly(String resourcePath, int size) throws IOException, DocumentException {

        Resource resource = new FileSystemResource(resourcePath);
        FileInputStream inputStream = new FileInputStream(resource.getFile());

        Document pdf = new Document();
        try (ByteArrayOutputStream arrayOutputStream = new ByteArrayOutputStream()) {
            PdfWriter.getInstance(pdf, arrayOutputStream);
            pdf.open();

            msgDocumentConverter.convertToPdf(pdf, inputStream);

            pdf.close();
            inputStream.close();
            assertEquals(size, arrayOutputStream.toByteArray().length);

            //Write the file to the project root, so we can inspect it if we want
            FileOutputStream fos = new FileOutputStream("sample." + resource.getFilename() + ".pdf");
            fos.write(arrayOutputStream.toByteArray());
            fos.flush();
            fos.close();

        }
    }

    @Test
    public void checkSupport() {
        assertTrue(msgDocumentConverter.isSupported("msg"));
        assertTrue(msgDocumentConverter.isSupported("MSG"));

        assertFalse(msgDocumentConverter.isSupported("jpg"));
        assertFalse(msgDocumentConverter.isSupported("png"));
        assertFalse(msgDocumentConverter.isSupported("tif"));
        assertFalse(msgDocumentConverter.isSupported("gif"));
        assertFalse(msgDocumentConverter.isSupported("jpeg"));
        assertFalse(msgDocumentConverter.isSupported("tiff"));
        assertFalse(msgDocumentConverter.isSupported("doc"));
        assertFalse(msgDocumentConverter.isSupported("rtf"));
        assertFalse(msgDocumentConverter.isSupported("txt"));
        assertFalse(msgDocumentConverter.isSupported("docx"));
        assertFalse(msgDocumentConverter.isSupported("pdf"));
    }

    @Test
    public void extractContents_sample4() throws IOException, DocumentException {
        FileInputStream fileInputStream = new FileInputStream("src/test/resources/testdata/sample4.MSG");

        MsgContents result = msgDocumentConverter.extractContents(fileInputStream);

        assertEquals("Sat Aug 12 19:25:25 BST 2006", result.getSentOn());
        assertEquals(142, result.getBodyHTML().length());
        assertEquals(150, result.getBodyText().length());
        assertEquals("John Doe", result.getFromName());
        assertEquals("testadd@example.org", result.getToEmail());
        assertEquals("testadd@example.org", result.getToName());
        // this email subject is long because it has been inserted into an existing sample .msg file. Changing the
        // length breaks the checksum
        assertEquals("Email Subject Email Subject Email Subject Email Subject Email Subj.", result.getSubject());

    }
}
