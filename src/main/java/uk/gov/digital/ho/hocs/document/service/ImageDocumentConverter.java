package uk.gov.digital.ho.hocs.document.service;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

@Service
class ImageDocumentConverter {

    private static final float PDF_WIDTH = 595;
    private static final float PDF_HEIGHT = 842;

    private static final String TIF_EXT = "tif";
    private static final String TIFF_EXT = "tiff";
    private static final String JPG_EXT = "jpg";
    private static final String JPEG_EXT = "jpeg";
    private static final String GIF_EXT = "gif";
    private static final String PNG_EXT = "png";

    private static final String[] SUPPORTED_EXTENSIONS = {TIF_EXT, TIFF_EXT, JPEG_EXT, JPG_EXT, GIF_EXT, PNG_EXT};

    public boolean isSupported(String fileExtension) {
        return Arrays.stream(SUPPORTED_EXTENSIONS).anyMatch(it -> it.equalsIgnoreCase(fileExtension));
    }

    public void convertToPdf(PDDocument pdf, InputStream inputStream) throws IOException {
        PDImageXObject img = PDImageXObject.createFromByteArray(pdf, inputStream.readAllBytes(), null);
        PDPage page = new PDPage(new PDRectangle(PDF_WIDTH, PDF_HEIGHT));
        pdf.addPage(page);
        PDPageContentStream contentStream = new PDPageContentStream(pdf, page);

        float width = img.getWidth();
        float height = img.getHeight();

        if (width > PDF_WIDTH) {
            width = PDF_WIDTH;
            height = height * (PDF_WIDTH / width);
        }

        if (height > PDF_HEIGHT) {
            height = PDF_HEIGHT;
            width = width * (PDF_HEIGHT / height);
        }

        contentStream.drawImage(img, 0, 0, width, height);
        contentStream.close();
    }
}
