package uk.gov.digital.ho.hocs.document;

import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Image;
import com.itextpdf.text.Jpeg;
import com.itextpdf.text.io.RandomAccessSourceFactory;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.text.pdf.RandomAccessFileOrArray;
import com.itextpdf.text.pdf.codec.GifImage;
import com.itextpdf.text.pdf.codec.PngImage;
import com.itextpdf.text.pdf.codec.TiffImage;
import org.apache.commons.io.IOUtils;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * This class was added because JOD Converter (the implemented converter) is not functioning well with
 * multi-page TIFF file conversions.
 * <p>
 * It uses the iText converter, which creates a new PDF and then adds the pages from the input file.
 * In testing it was able to convert TIFF files successfully that were not converted by JOD.
 * <p>
 * Furthermore, this module also takes care of shortcutting the process of converting a PDF.
 * It just copies the current contents out the stream again, so that no conversion takes place.
 * <p>
 * The immediate requirement is to convert the TIF and PDF files, but moving forward we may add
 * other extensions that JOD does not convert correctly.
 * <p>
 * iText supports PNG, GIF, JPG, JBIG, TIFF
 *
 * Updated 3 Feb 2020:
 * Moved the conversion of the following formats from JOD to iTextPdf : PNG, GIF, JPG & TIFF
 */

@Component
public class ExtendedDocumentConverter {

    private static final int PDF_WIDTH = 595;
    private static final int PDF_HEIGHT = 842;

    private static final String TIF_EXT = "tif";
    private static final String PDF_EXT = "pdf";
    private static final String JPG_EXT = "jpg";
    private static final String GIF_EXT = "gif";
    private static final String PNG_EXT = "png";

    private static final String TIFF_EXT = "tiff";
    private static final String JPEG_EXT = "jpeg";

    private static final String[] SUPPORTED_EXTENSIONS = { TIF_EXT, TIFF_EXT, PDF_EXT, JPEG_EXT, JPG_EXT, GIF_EXT, PNG_EXT };


    boolean isSupported(String fileExtension) {
        if (fileExtension == null || fileExtension.trim().isEmpty()) {
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
        if (TIF_EXT.equalsIgnoreCase(ext) || TIFF_EXT.equalsIgnoreCase(ext)) {
            return convertTiff(inputStream);
        }
        if (JPG_EXT.equalsIgnoreCase(ext) || JPEG_EXT.equalsIgnoreCase(ext)) {
            return convertJpeg(inputStream);
        }
        if (GIF_EXT.equalsIgnoreCase(ext)) {
            return convertGif(inputStream);
        }
        if (PNG_EXT.equalsIgnoreCase(ext)) {
            return convertPng(inputStream);
        }
        if (PDF_EXT.equalsIgnoreCase(ext)) {
            return copyPdf(inputStream);
        }
        throw new DocumentException("Unsupported format for conversion");
    }


    private byte[] convertTiff(InputStream inputStream) throws IOException, DocumentException {

        byte[] data = IOUtils.toByteArray(inputStream);
        RandomAccessFileOrArray tiffFile = new RandomAccessFileOrArray(new RandomAccessSourceFactory().createSource(data));
        int numberOfPages = TiffImage.getNumberOfPages(tiffFile);

        Document pdf = new Document();
        final ByteArrayOutputStream arrayOutputStream = new ByteArrayOutputStream();
        PdfWriter.getInstance(pdf, arrayOutputStream);
        pdf.open();

        for (int i = 1 ; i <= numberOfPages ; i++) {
            pdf.newPage();
            Image tempImage = TiffImage.getTiffImage(tiffFile, i);
            tempImage.setAbsolutePosition(0, 0);
            tempImage.scaleToFit(PDF_WIDTH, PDF_HEIGHT);
            pdf.add(tempImage);
        }

        pdf.close();
        final byte[] outputBytes = arrayOutputStream.toByteArray();
        arrayOutputStream.close();
        return outputBytes;
    }


    private byte[] convertJpeg(InputStream inputStream) throws IOException, DocumentException {

        byte[] fileBytes = IOUtils.toByteArray(inputStream);

        Document pdf = new Document();
        final ByteArrayOutputStream arrayOutputStream = new ByteArrayOutputStream();
        PdfWriter.getInstance(pdf, arrayOutputStream);
        pdf.open();

        Image tempImage = new Jpeg(fileBytes);
        return getPdfBytes(pdf, arrayOutputStream, tempImage);
    }


    private byte[] convertGif(InputStream inputStream) throws IOException, DocumentException {

        byte[] fileBytes = IOUtils.toByteArray(inputStream);

        Document pdf = new Document();
        final ByteArrayOutputStream arrayOutputStream = new ByteArrayOutputStream();
        PdfWriter.getInstance(pdf, arrayOutputStream);
        pdf.open();

        GifImage gif = new GifImage(fileBytes);
        Image tempImage = gif.getImage(1);
        return getPdfBytes(pdf, arrayOutputStream, tempImage);
    }


    private byte[] convertPng(InputStream inputStream) throws IOException, DocumentException {

        byte[] fileBytes = IOUtils.toByteArray(inputStream);

        Document pdf = new Document();
        final ByteArrayOutputStream arrayOutputStream = new ByteArrayOutputStream();
        PdfWriter.getInstance(pdf, arrayOutputStream);
        pdf.open();

        Image tempImage = PngImage.getImage(fileBytes);
        return getPdfBytes(pdf, arrayOutputStream, tempImage);
    }


    private byte[] getPdfBytes(Document pdf, ByteArrayOutputStream arrayOutputStream, Image tempImage) throws DocumentException, IOException {
        if (tempImage == null) {
            arrayOutputStream.close();
            pdf.newPage();
            pdf.close();
            return null;
        }
        pdf.newPage();
        tempImage.setAbsolutePosition(0, 0);
        tempImage.scaleToFit(PDF_WIDTH, PDF_HEIGHT);
        pdf.add(tempImage);

        pdf.close();
        final byte[] outputBytes = arrayOutputStream.toByteArray();
        arrayOutputStream.close();
        return outputBytes;
    }


    private byte[] copyPdf(InputStream inputStream) throws IOException {
        return IOUtils.toByteArray(inputStream);
    }

}
