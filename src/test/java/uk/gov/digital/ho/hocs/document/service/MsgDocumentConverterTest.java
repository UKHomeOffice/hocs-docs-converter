package uk.gov.digital.ho.hocs.document.service;


import org.apache.commons.lang3.StringUtils;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
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
import java.util.List;
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
                Arguments.of("src/test/resources/testdata/sample1.msg", 4),
                Arguments.of("src/test/resources/testdata/sample2.msg", 4),
                Arguments.of("src/test/resources/testdata/sample3.msg", 4),
                Arguments.of("src/test/resources/testdata/sample4.MSG", 1)
        );
    }

    @ParameterizedTest
    @MethodSource("getFiles")
    public void textExtendedDocumentConverterTiffDirectly(String resourcePath, int pages) throws IOException {

        Resource resource = new FileSystemResource(resourcePath);
        FileInputStream inputStream = new FileInputStream(resource.getFile());

        PDDocument pdf = new PDDocument();
        try (ByteArrayOutputStream arrayOutputStream = new ByteArrayOutputStream()) {

            msgDocumentConverter.convertToPdf(pdf, inputStream);

            pdf.close();
            inputStream.close();
            assertEquals(pages, pdf.getNumberOfPages());

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
    public void extractContents_sample4() throws IOException {
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

    @Test
    public void testDocumentContentsParseCorrectly() throws IOException {
        String fromEmail = "from@email.test";
        String fromName = "from name";
        String subject = "subject";
        String bodyHTML = "aaaaaaaaaaaa This is body text that should be split over two lines due to its length. bbbbbbbbbbbb This is body text that should be split over two lines due to its length.";
        String bodyText = "aaaaaaaaaaaa This is body text that should be split over two lines due to its length. bbbbbbbbbbbb This is body text that should be split over two lines due to its length.";
        String toEmail = "to@email.test";
        String toName = "to name";
        String sentOn = "Sat Aug 12 19:25:25 BST 2006";

        MsgContents contents = new MsgContents();
        contents.setFromEmail(fromEmail);
        contents.setFromName(fromName);
        contents.setSubject(subject);
        contents.setBodyHTML(bodyHTML);
        contents.setBodyText(bodyText);
        contents.setToEmail(toEmail);
        contents.setToName(toName);
        contents.setSentOn(sentOn);

        PDDocument pdf = new PDDocument();
        PDPage page = new PDPage();
        pdf.addPage(page);

        List<String> lines = msgDocumentConverter.parseElements(contents, pdf);

        assertEquals(String.format("From: %s [%s]", fromEmail, contents.getFromName()), lines.get(0));
        assertEquals(String.format("To: %s", toEmail), lines.get(1));
        assertEquals(String.format("Subject: %s", subject), lines.get(2));
        assertEquals(String.format("Sent on: %s", sentOn), lines.get(3));
        assertEquals("aaaaaaaaaaaa This is body text that should be split over two lines due to its length.", lines.get(4));
        assertEquals("bbbbbbbbbbbb This is body text that should be split over two lines due to its length.", lines.get(5));
    }

    @Test
    public void testDocumentsPaginateCorrectly() throws IOException {
        PDDocument pdf = new PDDocument();
        PDPage page = new PDPage();
        pdf.addPage(page);

        PDPageContentStream contentStream = new PDPageContentStream(pdf, page, PDPageContentStream.AppendMode.APPEND, true, true);

        List<String> lines = List.of(
                "00", "01", "02", "03", "04", "05", "06", "07", "08", "09",
                "10", "11", "12", "13", "14", "15", "16", "17", "18", "19",
                "20", "21", "22", "23", "24", "25", "26", "27", "28", "29",
                "30", "31", "32", "33", "34", "35", "36", "37", "38", "39"
        );

        msgDocumentConverter.printContents(contentStream, lines, pdf);

        assertEquals(2, pdf.getNumberOfPages());
    }
}
