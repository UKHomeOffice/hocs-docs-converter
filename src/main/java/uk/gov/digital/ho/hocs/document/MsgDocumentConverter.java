package uk.gov.digital.ho.hocs.document;

import com.itextpdf.text.*;
import com.itextpdf.tool.xml.ElementList;
import com.itextpdf.tool.xml.XMLWorkerHelper;
import com.itextpdf.tool.xml.exceptions.RuntimeWorkerException;
import org.apache.commons.lang3.StringUtils;
import org.simplejavamail.outlookmessageparser.OutlookMessageParser;
import org.simplejavamail.outlookmessageparser.model.OutlookAttachment;
import org.simplejavamail.outlookmessageparser.model.OutlookFileAttachment;
import org.simplejavamail.outlookmessageparser.model.OutlookMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.MessageFormat;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

/**
 * This class uses the same process as in ExtendedDocumentConverter, ie iTextPdf, for converting MSG files,
 * with the added difference of the Msg Parser and iTextPdf Tool components.
 **/

@Component
public class MsgDocumentConverter {


    @Autowired
    public MsgDocumentConverter(ImageDocumentConverter imageDocumentConverter) {
        this.imageDocumentConverter = imageDocumentConverter;
    }

    ImageDocumentConverter imageDocumentConverter;

    private static final String MSG_EXT = "msg";

    private static final String[] SUPPORTED_EXTENSIONS = { MSG_EXT };


    boolean isSupported(String fileExtension) {
        if (StringUtils.isEmpty(fileExtension)) {
            return false;
        }
        final String extension = fileExtension.trim();
        for (String ext : SUPPORTED_EXTENSIONS) {
            if (ext.equalsIgnoreCase(extension)) {
                return true;
            }
        }
        return false;
    }

    Document convertToPdf(Document pdf, String ext, InputStream inputStream) throws IOException, DocumentException {
        if (MSG_EXT.equalsIgnoreCase(ext)) {
            return convertMsg(pdf, inputStream);
        }
        throw new DocumentException("Unsupported format for conversion");
    }


    private Document convertMsg(Document pdf, InputStream inputStream) throws IOException, DocumentException {

        OutlookMessageParser parser = new OutlookMessageParser();
        OutlookMessage message = parser.parseMsg(inputStream);
        MsgContents contents = extractContents(message);

        pdf.newPage();

        pdf.add(new Paragraph(String.format("From: %s [%s]", contents.getFromEmail(),
                                                 StringUtils.isEmpty(contents.getFromName()) ? "N/A" : contents.getFromName())));
        pdf.add(new Paragraph(MessageFormat.format("To: {0}", StringUtils.isEmpty(contents.getToEmail()) ?
                                                                   contents.getToName() : contents.getToEmail())));
        pdf.add(new Paragraph("Subject: " + contents.getSubject()));

        pdf.add(new Paragraph("Sent on: " + contents.getSentOn()));

        if (!parseElements(pdf, contents)) {
            pdf.add(new Paragraph(""));
            pdf.add(new Paragraph(contents.getBodyText()));
        }
        for (OutlookAttachment attachment : contents.getAttachments()) {
            processAttachment(pdf, attachment);
        }

        return pdf;
    }

    public MsgContents extractContents(OutlookMessage message) throws DocumentException {
        if (message == null) {
            throw new DocumentException("Invalid MSG Contents");
        }
        MsgContents contents = new MsgContents();
        contents.setFromEmail(StringUtils.defaultString(message.getFromEmail(), StringUtils.EMPTY));
        contents.setFromName(StringUtils.defaultString(message.getFromName(), StringUtils.EMPTY));

        if (!message.getToRecipients().isEmpty()) {
            if (message.getToRecipients().get(0).getAddress() != null) {
                contents.setToEmail(StringUtils
                        .defaultString(message.getToRecipients().get(0).getAddress(), StringUtils.EMPTY));
            }
            if (message.getToRecipients().get(0).getName() != null) {
                contents.setToName(StringUtils
                        .defaultString(message.getToRecipients().get(0).getName(), StringUtils.EMPTY));
            }
        }

        contents.setBodyText(StringUtils.defaultString(message.getBodyText(), StringUtils.EMPTY));
        contents.setSubject(StringUtils.defaultString(message.getSubject(), StringUtils.EMPTY));
        contents.setBodyHTML(StringUtils.defaultString(message.getConvertedBodyHTML(), StringUtils.EMPTY));

        var zonedDateTime = message.getDate().toInstant().atZone(ZoneId.of("Europe/London"));
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("EEE MMM dd HH:mm:ss z yyyy");
        contents.setSentOn(zonedDateTime.format(formatter));

        if (message.getOutlookAttachments() != null) {
            contents.setAttachments(message.getOutlookAttachments());
        }
        return contents;
    }

    private boolean parseElements(Document document, MsgContents contents) {
        if (StringUtils.isEmpty(contents.getBodyHTML())) {
            return false;
        }
        try {
            ElementList elements = XMLWorkerHelper.parseToElementList(contents.getBodyHTML(), null);
            if (!elements.isEmpty()) {
                document.add(new Paragraph(""));
                for (Element element : elements) {
                    document.add(element);
                }
            }
        } catch (RuntimeWorkerException | DocumentException | IOException e) {
            return false;
        }
        return true;
    }

    private void processAttachment(Document pdf, OutlookAttachment attachment) throws DocumentException, IOException {
        // we don't process messages containing messages
        if (!(attachment instanceof OutlookFileAttachment)) {
            return;
        }
        OutlookFileAttachment fileAttachment = (OutlookFileAttachment)attachment;
        imageDocumentConverter.convertToPdf(pdf,
                StringUtils.remove(fileAttachment.getExtension(),"."),
                new ByteArrayInputStream(fileAttachment.getData()));
    }
}
