package uk.gov.digital.ho.hocs.document;

import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Image;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.text.pdf.RandomAccessFileOrArray;
import com.itextpdf.text.pdf.codec.TiffImage;
import org.apache.commons.io.IOUtils;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 *  This class was added because JOD Converter (the implemented converter) is not functioning well with
 *  multi-page file conversions.
 *
 *  It used the iText converter, which creates a new PDF and then adds the pages from the input file.
 *  In testing, it was able to convert files successfully that were not converted by JOD.
 *
 *  Furthermore, this module also takes care of shortcutting the process of converting a PDF.
 *  It just copies the current contents out the stream again, so that no conversion takes place.
 *
 *  The immediate requirement is to convert the TIF and PDF files, but moving forward we may add
 *  other extensions that JOD does not convert correctly.
 *
 */

@Component
public class ExtendedDocumentConverter {

    private static final int PDF_WIDTH = 595;
    private static final int PDF_HEIGHT = 842;

    private static final String TIF_EXT = "tif";
    private static final String PDF_EXT = "pdf";
    private static final String TIFF_EXT = "tiff";

    private static final String[] SUPPORTED_EXTENSIONS = { TIF_EXT, TIFF_EXT, PDF_EXT };


    boolean isSupported(String fileExtension) {
        if (fileExtension == null || fileExtension.isEmpty()) {
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
        if (PDF_EXT.equalsIgnoreCase(ext)) {
            return copyPdf(inputStream);
        }
        throw new DocumentException("Unsupported format for conversion");
    }

    private byte[] convertTiff(InputStream inputStream) throws IOException, DocumentException {

        byte[] fileBytes = IOUtils.toByteArray(inputStream);
        RandomAccessFileOrArray tiffFile = new RandomAccessFileOrArray(fileBytes);
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


    private byte[] copyPdf(InputStream inputStream) throws IOException {
        return IOUtils.toByteArray(inputStream);
    }

}
