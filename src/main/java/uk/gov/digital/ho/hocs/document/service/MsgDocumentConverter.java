package uk.gov.digital.ho.hocs.document.service;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.simplejavamail.outlookmessageparser.OutlookMessageParser;
import org.simplejavamail.outlookmessageparser.model.OutlookAttachment;
import org.simplejavamail.outlookmessageparser.model.OutlookFileAttachment;
import org.simplejavamail.outlookmessageparser.model.OutlookMessage;
import org.springframework.stereotype.Service;
import uk.gov.digital.ho.hocs.document.application.exception.ApplicationExceptions;
import uk.gov.digital.ho.hocs.document.domain.MsgContents;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static net.logstash.logback.argument.StructuredArguments.value;
import static uk.gov.digital.ho.hocs.document.application.LogEvent.DOCUMENT_CONVERSION_MSG_ATTACHMENT_PARSE_FAILURE;
import static uk.gov.digital.ho.hocs.document.application.LogEvent.DOCUMENT_CONVERSION_MSG_PARSE_FAILURE;
import static uk.gov.digital.ho.hocs.document.application.LogEvent.EVENT;
import static uk.gov.digital.ho.hocs.document.application.LogEvent.EXCEPTION;

@Slf4j
@Service
class MsgDocumentConverter {

    private final ImageDocumentConverter imageDocumentConverter;

    private final float fontSize = 12;
    private final float margin = 72;
    private final float leading = 1.5f * fontSize;
    private final PDFont font = new PDType1Font(Standard14Fonts.FontName.HELVETICA);

    private static final String FROM_STRING = "From: %s [%s]";
    private static final String TO_STRING = "To: %s";
    private static final String SUBJECT_STRING = "Subject: %s";
    private static final String SENT_ON_STRING = "Sent on: %s";

    public MsgDocumentConverter(ImageDocumentConverter imageDocumentConverter) {
        this.imageDocumentConverter = imageDocumentConverter;
    }

    public boolean isSupported(String fileExtension) {
        return "msg".equalsIgnoreCase(fileExtension);
    }

    public void convertToPdf(PDDocument pdf, InputStream inputStream) throws IOException {
        MsgContents contents = extractContents(inputStream);

        PDPage page = new PDPage();
        pdf.addPage(page);
        List<String> pdfContents = parseElements(contents, pdf);

        try (PDPageContentStream contentStream = new PDPageContentStream(pdf, page, PDPageContentStream.AppendMode.APPEND, true, true)){
            printContents(contentStream, pdfContents, pdf);
        } catch (IOException e) {
            log.warn("Failed to print MSG document: {}", e.getMessage(), value(EVENT, DOCUMENT_CONVERSION_MSG_PARSE_FAILURE), value(EXCEPTION, e));
            throw e;
        }

        for (OutlookAttachment attachment : contents.getAttachments()) {
            processAttachment(pdf, attachment);
        }
    }

    public MsgContents extractContents(InputStream inputStream) throws IOException {
        OutlookMessage message = new OutlookMessageParser().parseMsg(inputStream);

        if (message == null) {
            throw new ApplicationExceptions.DocumentConversionException("Invalid MSG Contents", DOCUMENT_CONVERSION_MSG_PARSE_FAILURE);
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

    public List<String> parseElements(MsgContents contents, PDDocument pdf) throws IOException {

        try {
            ArrayList<String> paragraphs = new ArrayList<>(List.of(
                    String.format(FROM_STRING, contents.getFromEmail(), StringUtils.isEmpty(contents.getFromName()) ? "N/A" : contents.getFromName()),
                    String.format(TO_STRING, StringUtils.isEmpty(contents.getToEmail()) ? contents.getToName() : contents.getToEmail()),
                    String.format(SUBJECT_STRING, contents.getSubject().replaceAll("\\P{Print}", "")),
                    String.format(SENT_ON_STRING, contents.getSentOn())
            ));

            List<String> bodyParagraphs = Arrays.stream(contents.getBodyText().split("\r\n")).map(p -> p.replaceAll("\\P{Print}", "")).collect(Collectors.toList());

            paragraphs.addAll(bodyParagraphs);

            PDRectangle mediabox = pdf.getPage(0).getMediaBox();
            float width = mediabox.getWidth() - 2 * margin;

            List<String> lines = new ArrayList<>();

            for (String paragraph : paragraphs) {
                if (paragraph.length() == 0) {
                    lines.add("");
                }
                int lastSpace = -1;
                while (paragraph.length() > 0) {
                    int spaceIndex = paragraph.indexOf(' ', lastSpace + 1);
                    if (spaceIndex < 0)
                        spaceIndex = paragraph.length();
                    String subString = paragraph.substring(0, spaceIndex);
                    float size = fontSize * font.getStringWidth(subString) / 1000;
                    if (size > width) {
                        if (lastSpace < 0)
                            lastSpace = spaceIndex;
                        subString = paragraph.substring(0, lastSpace);
                        lines.add(subString);
                        paragraph = paragraph.substring(lastSpace).trim();
                        lastSpace = -1;
                    } else if (spaceIndex == paragraph.length()) {
                        lines.add(paragraph);
                        paragraph = "";
                    } else {
                        lastSpace = spaceIndex;
                    }
                }
            }
            return lines;
        } catch (IOException e) {
            log.warn("Failed to Parse MSG Elements: {}", e.getMessage(), value(EVENT, DOCUMENT_CONVERSION_MSG_PARSE_FAILURE), value(EXCEPTION, e));
            throw e;
        }
    }

    public void printContents(PDPageContentStream contentStream, List<String> lines, PDDocument pdf) throws IOException {
        PDRectangle mediabox = pdf.getPage(0).getMediaBox();
        float height = mediabox.getHeight() - 2 * margin;

        int lineOnPage = 0;
        contentStream.beginText();
        contentStream.setFont(font, fontSize);
        contentStream.setLeading(leading);
        contentStream.newLineAtOffset(margin, 700);

        for (String line: lines)
        {
            contentStream.showText(line);
            contentStream.newLineAtOffset(0, -leading);
            lineOnPage++;
            if (lineOnPage * leading > height) {
                contentStream.endText();
                contentStream.close();

                PDPage page = new PDPage();
                pdf.addPage(page);
                contentStream = new PDPageContentStream(pdf, page, PDPageContentStream.AppendMode.APPEND, true, true);

                contentStream.beginText();
                contentStream.setFont(font, fontSize);
                contentStream.setLeading(leading);
                contentStream.newLineAtOffset(margin, 700);

                lineOnPage = 0;
            }
        }

        contentStream.endText();
        contentStream.close();
    }

    private void processAttachment(PDDocument pdf, OutlookAttachment attachment) throws IOException {
        // we don't process messages containing messages
        if (!(attachment instanceof OutlookFileAttachment)) {
            return;
        }
        OutlookFileAttachment fileAttachment = (OutlookFileAttachment) attachment;
        // Only process attachments if they are images
        try {
            imageDocumentConverter.convertToPdf(pdf,
                    new ByteArrayInputStream(fileAttachment.getData()));
        } catch (Exception ex) {
            log.warn("Failed to process attachment for conversion.", ex.getMessage(),
                    value(EVENT, DOCUMENT_CONVERSION_MSG_ATTACHMENT_PARSE_FAILURE), value(EXCEPTION, ex));
        }
    }
}
