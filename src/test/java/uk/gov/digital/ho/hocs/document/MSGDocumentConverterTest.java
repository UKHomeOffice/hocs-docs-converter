package uk.gov.digital.ho.hocs.document;


import com.itextpdf.text.DocumentException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.simplejavamail.outlookmessageparser.OutlookMessageParser;
import org.simplejavamail.outlookmessageparser.model.OutlookMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.File;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest(webEnvironment = RANDOM_PORT)
public class MSGDocumentConverterTest {

    @Autowired
    private MSGDocumentConverter msgDocumentConverter;

    @Test
    public void extractContents_sample4() throws IOException, DocumentException {
        String path = "src/test/resources/testdata/sample4.msg";

        File file = new File(path);

        OutlookMessageParser parser = new OutlookMessageParser();
        OutlookMessage message = parser.parseMsg(file);

        MsgContents result = msgDocumentConverter.extractContents(message);

        assertEquals(result.getSentOn(), "Sat Aug 12 19:25:25 BST 2006");
        assertEquals(result.getBodyHTML().length(), 142);
        assertEquals(result.getBodyText().length(), 150);
        assertEquals(result.getFromName(), "John Doe");
        assertEquals(result.getToEmail(), "testadd@example.org");
        assertEquals(result.getToName(), "testadd@example.org");
        // this email subject is long because it has been inserted into an existing sample .msg file. Changing the
        // length breaks the checksum
        assertEquals(result.getSubject(), "Email Subject Email Subject Email Subject Email Subject Email Subj.");

    }
}