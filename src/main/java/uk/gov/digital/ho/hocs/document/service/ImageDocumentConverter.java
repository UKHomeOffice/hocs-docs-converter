package uk.gov.digital.ho.hocs.document.service;

import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Image;
import com.itextpdf.text.Jpeg;
import com.itextpdf.text.io.RandomAccessSource;
import com.itextpdf.text.io.RandomAccessSourceFactory;
import com.itextpdf.text.pdf.RandomAccessFileOrArray;
import com.itextpdf.text.pdf.codec.GifImage;
import com.itextpdf.text.pdf.codec.PngImage;
import com.itextpdf.text.pdf.codec.TiffImage;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

class ImageDocumentConverter {

    private static final int PDF_WIDTH = 595;
    private static final int PDF_HEIGHT = 842;

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

    public void convertToPdf(Document pdf, String ext, InputStream inputStream) throws DocumentException, IOException {

        List<Image> images = new ArrayList<>();

        if (TIF_EXT.equalsIgnoreCase(ext) || TIFF_EXT.equalsIgnoreCase(ext)) {
            images.addAll(convertTiff(inputStream));
        } else if (JPG_EXT.equalsIgnoreCase(ext) || JPEG_EXT.equalsIgnoreCase(ext)) {
            Image img = new Jpeg(inputStream.readAllBytes());
            images.add(img);
        } else if (GIF_EXT.equalsIgnoreCase(ext)) {
            GifImage gif = new GifImage(inputStream.readAllBytes());
            images.add(gif.getImage(1));
        } else if (PNG_EXT.equalsIgnoreCase(ext)) {
            Image img = PngImage.getImage(inputStream.readAllBytes());
            images.add(img);
        }

        for (Image image : images) {
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

    private List<Image> convertTiff(InputStream inputStream) throws IOException {
        RandomAccessSource source = new RandomAccessSourceFactory().createSource(inputStream.readAllBytes());
        RandomAccessFileOrArray tiffFile = new RandomAccessFileOrArray(source);
        int numberOfPages = TiffImage.getNumberOfPages(tiffFile);

        List<Image> images = new ArrayList<>(numberOfPages);
        for (int i = 1; i <= numberOfPages; i++) {
            images.add(TiffImage.getTiffImage(tiffFile, i));
        }
        return images;
    }
}
