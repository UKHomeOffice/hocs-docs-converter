package uk.gov.digital.ho.hocs.document.service;

import com.itextpdf.text.*;
import com.itextpdf.tool.xml.ElementList;
import com.itextpdf.tool.xml.XMLWorkerHelper;
import com.itextpdf.tool.xml.exceptions.RuntimeWorkerException;
import org.apache.commons.lang3.StringUtils;
import org.simplejavamail.outlookmessageparser.OutlookMessageParser;
import org.simplejavamail.outlookmessageparser.model.OutlookAttachment;
import org.simplejavamail.outlookmessageparser.model.OutlookFileAttachment;
import org.simplejavamail.outlookmessageparser.model.OutlookMessage;
import uk.gov.digital.ho.hocs.document.domain.MsgContents;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.MessageFormat;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
class MsgDocumentConverter {

    ImageDocumentConverter imageDocumentConverter = new ImageDocumentConverter();

    private static final String MSG_EXT = "msg";

    private static final String[] SUPPORTED_EXTENSIONS = { MSG_EXT };

    public boolean isSupported(String fileExtension) {
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

    public void convertToPdf(Document pdf, InputStream inputStream) throws DocumentException, IOException {
        MsgContents contents = extractContents(inputStream);

        pdf.newPage();

        pdf.add(new Paragraph(String.format("From: %s [%s]", contents.getFromEmail(), StringUtils.isEmpty(contents.getFromName()) ? "N/A" : contents.getFromName())));
        pdf.add(new Paragraph(MessageFormat.format("To: {0}", StringUtils.isEmpty(contents.getToEmail()) ? contents.getToName() : contents.getToEmail())));
        pdf.add(new Paragraph("Subject: " + contents.getSubject()));
        pdf.add(new Paragraph("Sent on: " + contents.getSentOn()));

        if (!parseElements(pdf, contents)) {
            pdf.add(new Paragraph(""));
            pdf.add(new Paragraph(contents.getBodyText()));
        }

        for (OutlookAttachment attachment : contents.getAttachments()) {
            processAttachment(pdf, attachment);
        }
    }

    public MsgContents extractContents(InputStream inputStream) throws DocumentException, IOException {
        OutlookMessage message = new OutlookMessageParser().parseMsg(inputStream);

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
        OutlookFileAttachment fileAttachment = (OutlookFileAttachment) attachment;
        // Only process attachments if they are images
        imageDocumentConverter.convertToPdf(pdf,
                StringUtils.remove(fileAttachment.getExtension(),"."),
                new ByteArrayInputStream(fileAttachment.getData()));
    }
}