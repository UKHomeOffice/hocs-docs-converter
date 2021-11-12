package uk.gov.digital.ho.hocs.document;

import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Image;
import com.itextpdf.text.Jpeg;
import com.itextpdf.text.pdf.RandomAccessFileOrArray;
import com.itextpdf.text.pdf.codec.GifImage;
import com.itextpdf.text.pdf.codec.PngImage;
import com.itextpdf.text.pdf.codec.TiffImage;
import org.apache.commons.io.IOUtils;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

@Component
public class ImageDocumentConverter {

    private static final int PDF_WIDTH = 595;
    private static final int PDF_HEIGHT = 842;

    private static final String TIF_EXT = "tif";
    private static final String TIFF_EXT = "tiff";
    private static final String JPG_EXT = "jpg";
    private static final String JPEG_EXT = "jpeg";
    private static final String GIF_EXT = "gif";
    private static final String PNG_EXT = "png";


    private static final String[] SUPPORTED_EXTENSIONS = { TIF_EXT, TIFF_EXT, JPEG_EXT, JPG_EXT, GIF_EXT, PNG_EXT };

    public void convertToPdf(Document pdf, String ext, InputStream inputStream) throws DocumentException, IOException {

        List<Image> images = new ArrayList<>();

        if (TIF_EXT.equalsIgnoreCase(ext) || TIFF_EXT.equalsIgnoreCase(ext)) {
            images.addAll(convertTiff(inputStream));
        } else if (JPG_EXT.equalsIgnoreCase(ext) || JPEG_EXT.equalsIgnoreCase(ext)) {
            Image img = new Jpeg(IOUtils.toByteArray(inputStream));
            images.add(img);
        } else if (GIF_EXT.equalsIgnoreCase(ext)) {
            GifImage gif = new GifImage(IOUtils.toByteArray(inputStream));
            images.add(gif.getImage(1));
        } else if (PNG_EXT.equalsIgnoreCase(ext)) {
            Image img = PngImage.getImage(IOUtils.toByteArray(inputStream));
            images.add(img);
        }

        for(Image image: images) {
            if (image == null) {
                pdf.newPage();
            } else {
                pdf.newPage();
                image.setAbsolutePosition(0, 0);
                image.scaleToFit(PDF_WIDTH, PDF_HEIGHT);
                pdf.add(image);
            }
        }
    }

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

    private List<Image> convertTiff(InputStream inputStream) throws IOException, DocumentException {
        byte[] fileBytes = IOUtils.toByteArray(inputStream);
        RandomAccessFileOrArray tiffFile = new RandomAccessFileOrArray(fileBytes);
        int numberOfPages = TiffImage.getNumberOfPages(tiffFile);

        List<Image> images = new ArrayList<>(numberOfPages);
        for (int i = 1 ; i <= numberOfPages ; i++) {
            images.add(TiffImage.getTiffImage(tiffFile, i));
        }
        return images;
    }
}
