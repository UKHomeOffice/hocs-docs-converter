package uk.gov.digital.ho.hocs.document;


import com.itextpdf.text.DocumentException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.simplejavamail.outlookmessageparser.OutlookMessageParser;
import org.simplejavamail.outlookmessageparser.model.OutlookMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.File;
import java.io.IOException;

import static org.assertj.core.api.Java6Assertions.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
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

        assertThat(result.getSentOn()).isEqualTo("Sat Aug 12 19:25:25 BST 2006");
        assertThat(result.getBodyHTML().length()).isEqualTo(142);
        assertThat(result.getBodyText().length()).isEqualTo(150);
        assertThat(result.getFromName()).isEqualTo("John Doe");
        assertThat(result.getToEmail()).isEqualTo("testadd@example.org");
        assertThat(result.getToName()).isEqualTo("testadd@example.org");
        // this email subject is long because it has been inserted into an existing sample .msg file. Changing the
        // length breaks the checksum
        assertThat(result.getSubject()).isEqualTo("Email Subject Email Subject Email Subject Email Subject Email Subj.");

    }
}