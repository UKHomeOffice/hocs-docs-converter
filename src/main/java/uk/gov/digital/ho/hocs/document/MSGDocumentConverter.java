package uk.gov.digital.ho.hocs.document;

import com.auxilii.msgparser.Message;
import com.auxilii.msgparser.MsgParser;
import com.auxilii.msgparser.attachment.Attachment;
import com.auxilii.msgparser.attachment.FileAttachment;
import com.itextpdf.text.*;
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
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.MessageFormat;

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

        MsgParser parser = new MsgParser();
        Message message = parser.parseMsg(inputStream);
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
        for (Attachment attachment : contents.getAttachments()) {
            processAttachment(pdf, attachment);
        }
        pdf.close();

        final byte[] outputBytes = arrayOutputStream.toByteArray();
        arrayOutputStream.close();
        return outputBytes;
    }

    private MsgContents extractContents(Message message) throws DocumentException {
        if (message == null) {
            throw new DocumentException("Invalid MSG Contents");
        }
        MsgContents contents = new MsgContents();
        contents.setFromEmail(StringUtils.defaultString(message.getFromEmail(), StringUtils.EMPTY));
        contents.setFromName(StringUtils.defaultString(message.getFromName(), StringUtils.EMPTY));
        contents.setToEmail(StringUtils.defaultString(message.getToEmail(), StringUtils.EMPTY));
        contents.setToName(StringUtils.defaultString(message.getToName(), StringUtils.EMPTY));
        contents.setBodyText(StringUtils.defaultString(message.getBodyText(), StringUtils.EMPTY));
        contents.setSubject(StringUtils.defaultString(message.getSubject(), StringUtils.EMPTY));
        contents.setBodyHTML(StringUtils.defaultString(message.getConvertedBodyHTML(), StringUtils.EMPTY));
        contents.setSentOn(StringUtils.defaultString(String.valueOf(Message.getDateFromHeaders(message.getHeaders())), StringUtils.EMPTY));
        if (message.getAttachments() != null) {
            contents.setAttachments(message.getAttachments());
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
            for (Element element : elements) {
                document.add(element);
            }
        } catch (RuntimeWorkerException | DocumentException | IOException e) {
            return false;
        }
        return true;
    }

    private void processAttachment(Document pdf, Attachment attachment) {
        // we don't process messages containing messages
        if (!(attachment instanceof FileAttachment)) {
            return;
        }
        FileAttachment fileAttachment = (FileAttachment)attachment;
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
            log.info("Cannot convert attachment {} with error: {}", fileAttachment.getFilename(), e.getMessage(),
                     value(EVENT, DOCUMENT_CONVERSION_INVALID_FORMAT));
        }
    }

    private void addTiffToPdf(Document pdf, FileAttachment attachment) throws DocumentException {

        byte[] data = attachment.getData();
        RandomAccessFileOrArray tiffFile = new RandomAccessFileOrArray(data);
        int numberOfPages = TiffImage.getNumberOfPages(tiffFile);

        for (int i = 1 ; i <= numberOfPages ; i++) {
            Image image = TiffImage.getTiffImage(tiffFile, i);
            image.setAbsolutePosition(0, 0);
            image.scaleToFit(PDF_WIDTH, PDF_HEIGHT);
            pdf.newPage();
            pdf.add(image);
        }
    }

    private void addJpegToPdf(Document pdf, FileAttachment attachment) throws IOException, DocumentException {

        byte[] data = attachment.getData();
        Image image = new Jpeg(data);
        image.setAbsolutePosition(0, 0);
        image.scaleToFit(PDF_WIDTH, PDF_HEIGHT);

        pdf.newPage();
        pdf.add(image);

    }

    private void addPngToPdf(Document pdf, FileAttachment attachment) throws IOException, DocumentException {

        byte[] data = attachment.getData();
        Image image = PngImage.getImage(data);
        image.setAbsolutePosition(0, 0);
        image.scaleToFit(PDF_WIDTH, PDF_HEIGHT);

        pdf.newPage();
        pdf.add(image);

    }

    private void addGifToPdf(Document pdf, FileAttachment attachment) throws IOException, DocumentException {

        byte[] data = attachment.getData();
        GifImage gif = new GifImage(data);
        Image image = gif.getImage(1);
        image.setAbsolutePosition(0, 0);
        image.scaleToFit(PDF_WIDTH, PDF_HEIGHT);

        pdf.newPage();
        pdf.add(image);

    }

}
