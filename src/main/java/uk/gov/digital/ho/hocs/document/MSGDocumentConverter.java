package uk.gov.digital.ho.hocs.document;

import com.itextpdf.text.*;
import com.itextpdf.text.io.RandomAccessSourceFactory;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.text.pdf.RandomAccessFileOrArray;
import com.itextpdf.text.pdf.codec.GifImage;
import com.itextpdf.text.pdf.codec.PngImage;
import com.itextpdf.text.pdf.codec.TiffImage;
import com.itextpdf.tool.xml.ElementList;
import com.itextpdf.tool.xml.XMLWorkerHelper;
import com.itextpdf.tool.xml.exceptions.RuntimeWorkerException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.simplejavamail.outlookmessageparser.OutlookMessageParser;
import org.simplejavamail.outlookmessageparser.model.OutlookAttachment;
import org.simplejavamail.outlookmessageparser.model.OutlookFileAttachment;
import org.simplejavamail.outlookmessageparser.model.OutlookMessage;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.MessageFormat;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

import static net.logstash.logback.argument.StructuredArguments.value;
import static uk.gov.digital.ho.hocs.document.LogEvent.DOCUMENT_CONVERSION_INVALID_FORMAT;
import static uk.gov.digital.ho.hocs.document.LogEvent.EVENT;

/**
 * This class uses the same process as in ExtendedDocumentConverter, ie iTextPdf, for converting MSG files,
 * with the added difference of the Msg Parser and iTextPdf Tool components.
 **/

@Component
@Slf4j
public class MSGDocumentConverter {

    private static final int PDF_WIDTH = 595;
    private static final int PDF_HEIGHT = 842;

    private static final String MSG_EXT = "msg";

    private static final String TIF_EXT = "tif";
    private static final String JPG_EXT = "jpg";
    private static final String GIF_EXT = "gif";
    private static final String PNG_EXT = "png";

    private static final String TIFF_EXT = "tiff";
    private static final String JPEG_EXT = "jpeg";

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


    byte[] convertToPdf(String ext, InputStream inputStream) throws IOException, DocumentException {
        if (MSG_EXT.equalsIgnoreCase(ext)) {
            return convertMsg(inputStream);
        }
        throw new DocumentException("Unsupported format for conversion");
    }


    private byte[] convertMsg(InputStream inputStream) throws IOException, DocumentException {

        OutlookMessageParser parser = new OutlookMessageParser();
        OutlookMessage message = parser.parseMsg(inputStream);
        MsgContents contents = extractContents(message);

        Document pdf = new Document();
        final ByteArrayOutputStream arrayOutputStream = new ByteArrayOutputStream();
        PdfWriter.getInstance(pdf, arrayOutputStream);
        pdf.open();
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
        pdf.close();

        final byte[] outputBytes = arrayOutputStream.toByteArray();
        arrayOutputStream.close();
        return outputBytes;
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
            document.add(new Paragraph(""));

            if (elements.isEmpty()) {
                return false;
            }

            for (Element element : elements) {
                document.add(element);
            }
        } catch (RuntimeWorkerException | DocumentException | IOException e) {
            return false;
        }
        return true;
    }

    private void processAttachment(Document pdf, OutlookAttachment attachment) {
        // we don't process messages containing messages
        if (!(attachment instanceof OutlookFileAttachment)) {
            return;
        }
        OutlookFileAttachment fileAttachment = (OutlookFileAttachment)attachment;
        try {
            String fileExtension = StringUtils.remove(fileAttachment.getExtension(),".");
            if (TIF_EXT.equalsIgnoreCase(fileExtension) || TIFF_EXT.equalsIgnoreCase(fileExtension)) {
                addTiffToPdf(pdf, fileAttachment);
            }
            if (JPG_EXT.equalsIgnoreCase(fileExtension) || JPEG_EXT.equalsIgnoreCase(fileExtension)) {
                addJpegToPdf(pdf, fileAttachment);
            }
            if (PNG_EXT.equalsIgnoreCase(fileExtension)) {
                addPngToPdf(pdf, fileAttachment);
            }
            if (GIF_EXT.equalsIgnoreCase(fileExtension)) {
                addGifToPdf(pdf, fileAttachment);
            }
        } catch (Exception e) {
            log.info(String.format("Cannot convert attachment %s with error: %s" , fileAttachment.getFilename(), e.getMessage()), value(EVENT, DOCUMENT_CONVERSION_INVALID_FORMAT));
        }
    }

    private void addTiffToPdf(Document pdf, OutlookFileAttachment attachment) throws DocumentException {

        byte[] data = attachment.getData();
        RandomAccessFileOrArray tiffFile = new RandomAccessFileOrArray(new RandomAccessSourceFactory().createSource(data));
        int numberOfPages = TiffImage.getNumberOfPages(tiffFile);

        for (int i = 1 ; i <= numberOfPages ; i++) {
            Image image = TiffImage.getTiffImage(tiffFile, i);
            image.setAbsolutePosition(0, 0);
            image.scaleToFit(PDF_WIDTH, PDF_HEIGHT);
            pdf.newPage();
            pdf.add(image);
        }
    }

    private void addJpegToPdf(Document pdf, OutlookFileAttachment attachment) throws IOException, DocumentException {

        byte[] data = attachment.getData();
        Image image = new Jpeg(data);
        image.setAbsolutePosition(0, 0);
        image.scaleToFit(PDF_WIDTH, PDF_HEIGHT);

        pdf.newPage();
        pdf.add(image);

    }

    private void addPngToPdf(Document pdf, OutlookFileAttachment attachment) throws IOException, DocumentException {

        byte[] data = attachment.getData();
        Image image = PngImage.getImage(data);
        image.setAbsolutePosition(0, 0);
        image.scaleToFit(PDF_WIDTH, PDF_HEIGHT);

        pdf.newPage();
        pdf.add(image);

    }

    private void addGifToPdf(Document pdf, OutlookFileAttachment attachment) throws IOException, DocumentException {

        byte[] data = attachment.getData();
        GifImage gif = new GifImage(data);
        Image image = gif.getImage(1);
        image.setAbsolutePosition(0, 0);
        image.scaleToFit(PDF_WIDTH, PDF_HEIGHT);

        pdf.newPage();
        pdf.add(image);

    }

}
