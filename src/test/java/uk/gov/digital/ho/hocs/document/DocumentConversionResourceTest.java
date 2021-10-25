package uk.gov.digital.ho.hocs.document;

import com.itextpdf.text.DocumentException;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.*;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class DocumentConversionResourceTest {

    @Value("classpath:testdata/sample.docx")
    private Resource docx;

    @Value("classpath:testdata/sample.tif")
    private Resource tif;

    @Value("classpath:testdata/sample.gif")
    private Resource gif;

    @Value("classpath:testdata/sample.jpg")
    private Resource jpg;

    @Value("classpath:testdata/sample.png")
    private Resource png;

    @Value("classpath:testdata/sample.doc")
    private Resource doc;

    @Value("classpath:testdata/sample.pdf")
    private Resource pdf;

    @Value("classpath:testdata/sample.qt")
    private Resource qt;

    @Value("classpath:testdata/sample")
    private Resource none;

    @Value("classpath:converted/sample.tif.pdf")
    private Resource tifPdf;

    @Value("classpath:converted/sample.gif.pdf")
    private Resource gifPdf;

    @Value("classpath:converted/sample.png.pdf")
    private Resource pngPdf;

    @Value("classpath:converted/sample.jpg.pdf")
    private Resource jpgPdf;


    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private ExtendedDocumentConverter extendedDocumentConverter;

    @Test
    public void shouldReturn200ForValidFileUpload() throws IOException {

        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Type", "multipart/form-data");
        MultiValueMap<String, Object> map = new LinkedMultiValueMap<>();
        map.set("file", new FileSystemResource(docx.getFile()));

        ResponseEntity<String> response = restTemplate.exchange("/convert",
                HttpMethod.POST,
                new HttpEntity<>(map, headers),
                String.class);

        assertEquals(response.getStatusCode(), HttpStatus.OK);

    }

    @Test
    public void shouldReturn200ForTIF() throws IOException {

        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Type", "multipart/form-data");
        MultiValueMap<String, Object> map = new LinkedMultiValueMap<>();
        map.set("file", new FileSystemResource(tif.getFile()));

        ResponseEntity<String> response = restTemplate.exchange("/convert",
                HttpMethod.POST,
                new HttpEntity<>(map, headers),
                String.class);

        assertEquals(response.getStatusCode(), HttpStatus.OK);

    }

    @Test
    public void shouldReturn200ForJPEG() throws IOException {

        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Type", "multipart/form-data");
        MultiValueMap<String, Object> map = new LinkedMultiValueMap<>();
        map.set("file", new FileSystemResource(jpg.getFile()));

        ResponseEntity<String> response = restTemplate.exchange("/convert",
                HttpMethod.POST,
                new HttpEntity<>(map, headers),
                String.class);

        assertEquals(response.getStatusCode(), HttpStatus.OK);

    }

    @Test
    public void shouldReturn200ForDOC() throws IOException {

        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Type", "multipart/form-data");
        MultiValueMap<String, Object> map = new LinkedMultiValueMap<>();
        map.set("file", new FileSystemResource(doc.getFile()));

        ResponseEntity<String> response = restTemplate.exchange("/convert",
                HttpMethod.POST,
                new HttpEntity<>(map, headers),
                String.class);

        assertEquals(response.getStatusCode(), HttpStatus.OK);

    }

    @Test
    public void shouldReturn200ForDOCX() throws IOException {

        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Type", "multipart/form-data");
        MultiValueMap<String, Object> map = new LinkedMultiValueMap<>();
        map.set("file", new FileSystemResource(docx.getFile()));

        ResponseEntity<String> response = restTemplate.exchange("/convert",
                HttpMethod.POST,
                new HttpEntity<>(map, headers),
                String.class);

        assertEquals(response.getStatusCode(), HttpStatus.OK);
    }

    @Test
    public void shouldReturn200ForPDF() throws IOException {

        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Type", "multipart/form-data");
        MultiValueMap<String, Object> map = new LinkedMultiValueMap<>();
        map.set("file", new FileSystemResource(pdf.getFile()));

        ResponseEntity<String> response = restTemplate.exchange("/convert",
                HttpMethod.POST,
                new HttpEntity<>(map, headers),
                String.class);

        assertEquals(response.getStatusCode(), HttpStatus.OK);

    }

    @Test
    public void shouldReturn400ForBadFileExtensions() throws IOException {

        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Type", "multipart/form-data");
        MultiValueMap<String, Object> map = new LinkedMultiValueMap<>();
        map.set("file", new FileSystemResource(qt.getFile()));

        ResponseEntity<String> response = restTemplate.exchange("/convert",
                HttpMethod.POST,
                new HttpEntity<>(map, headers),
                String.class);

        assertEquals(response.getStatusCode(), HttpStatus.BAD_REQUEST);

    }

    @Test
    public void shouldReturn400ForEmptyFileExtensions() throws IOException {

        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Type", "multipart/form-data");
        MultiValueMap<String, Object> map = new LinkedMultiValueMap<>();
        map.set("file", new FileSystemResource(none.getFile()));

        ResponseEntity<String> response = restTemplate.exchange("/convert",
                HttpMethod.POST,
                new HttpEntity<>(map, headers),
                String.class);

        assertEquals(response.getStatusCode(), HttpStatus.BAD_REQUEST);

    }

    @Test
    public void testOkExtDocConverterTifAndContents() throws IOException {

        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Type", "multipart/form-data");
        MultiValueMap<String, Object> map = new LinkedMultiValueMap<>();
        map.set("file", new FileSystemResource(tif.getFile()));

        ResponseEntity<byte[]> response = restTemplate.exchange("/convert",
                                                                HttpMethod.POST,
                                                                new HttpEntity<>(map, headers),
                                                                byte[].class);

        assertEquals(response.getStatusCode(), HttpStatus.OK);

        final byte[] bytes = IOUtils.toByteArray(new FileInputStream(tifPdf.getFile()));
        assertEquals(bytes.length, response.getBody().length, 100);
    }

    @Test
    public void testOkExtDocConverterGifAndContents() throws IOException {

        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Type", "multipart/form-data");
        MultiValueMap<String, Object> map = new LinkedMultiValueMap<>();
        map.set("file", new FileSystemResource(gif.getFile()));

        ResponseEntity<byte[]> response = restTemplate.exchange("/convert",
                                                                HttpMethod.POST,
                                                                new HttpEntity<>(map, headers),
                                                                byte[].class);

        assertEquals(response.getStatusCode(), HttpStatus.OK);

        final byte[] bytes = IOUtils.toByteArray(new FileInputStream(gifPdf.getFile()));
        assertEquals(bytes.length, response.getBody().length, 100);
    }

    @Test
    public void testOkExtDocConverterJpgAndContents() throws IOException {

        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Type", "multipart/form-data");
        MultiValueMap<String, Object> map = new LinkedMultiValueMap<>();
        map.set("file", new FileSystemResource(jpg.getFile()));

        ResponseEntity<byte[]> response = restTemplate.exchange("/convert",
                                                                HttpMethod.POST,
                                                                new HttpEntity<>(map, headers),
                                                                byte[].class);

        assertEquals(response.getStatusCode(), HttpStatus.OK);

        final byte[] bytes = IOUtils.toByteArray(new FileInputStream(jpgPdf.getFile()));
        assertEquals(bytes.length, response.getBody().length, 100);
    }

    @Test
    public void testOkExtDocConverterPngAndContents() throws IOException {

        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Type", "multipart/form-data");
        MultiValueMap<String, Object> map = new LinkedMultiValueMap<>();
        map.set("file", new FileSystemResource(png.getFile()));

        ResponseEntity<byte[]> response = restTemplate.exchange("/convert",
                                                                HttpMethod.POST,
                                                                new HttpEntity<>(map, headers),
                                                                byte[].class);

        assertEquals(response.getStatusCode(), HttpStatus.OK);

        final byte[] bytes = IOUtils.toByteArray(new FileInputStream(pngPdf.getFile()));
        assertEquals(bytes.length, response.getBody().length, 100);
    }

    @Test
    public void testOkExtDocConverterPdfAndContents() throws IOException {

        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Type", "multipart/form-data");
        MultiValueMap<String, Object> map = new LinkedMultiValueMap<>();
        map.set("file", new FileSystemResource(pdf.getFile()));

        ResponseEntity<byte[]> converted = restTemplate.exchange("/convert",
                                                                HttpMethod.POST,
                                                                new HttpEntity<>(map, headers),
                                                                byte[].class);

        assertEquals(converted.getStatusCode(), HttpStatus.OK);

        final byte[] originalBytes = FileUtils.readFileToByteArray(pdf.getFile());
        assertArrayEquals(originalBytes, converted.getBody());
    }

    @Test
    public void textExtendedDocumentConverterTiffDirectly() throws IOException, DocumentException {
        FileInputStream inputStream = new FileInputStream(tif.getFile());
        final byte[] convertedBytes = extendedDocumentConverter.convertToPdf("tif", inputStream);
        inputStream.close();
        final byte[] pdfBytes = IOUtils.toByteArray(new FileInputStream(tifPdf.getFile()));
        assertEquals(pdfBytes.length, convertedBytes.length, 100);
    }

    @Test
    public void textExtendedDocumentConverterPdfDirectly() throws IOException, DocumentException {
        FileInputStream inputStream = new FileInputStream(pdf.getFile());
        final byte[] convertedBytes = extendedDocumentConverter.convertToPdf("pdf", inputStream);
        inputStream.close();
        final byte[] pdfBytes = IOUtils.toByteArray(new FileInputStream(pdf.getFile()));
        assertEquals(pdfBytes.length, convertedBytes.length);
        assertArrayEquals(pdfBytes, convertedBytes); // same file - same timestamp - can compare contents
    }

    @Test
    public void checkExtendedSupport() {
        assertTrue(extendedDocumentConverter.isSupported("pdf"));
        assertTrue(extendedDocumentConverter.isSupported("jpg"));
        assertTrue(extendedDocumentConverter.isSupported("png"));
        assertTrue(extendedDocumentConverter.isSupported("tif"));
        assertTrue(extendedDocumentConverter.isSupported("gif"));
        assertTrue(extendedDocumentConverter.isSupported("jpeg"));
        assertTrue(extendedDocumentConverter.isSupported("tiff"));

        assertFalse(extendedDocumentConverter.isSupported("doc"));
        assertFalse(extendedDocumentConverter.isSupported("rtf"));
        assertFalse(extendedDocumentConverter.isSupported("txt"));
        assertFalse(extendedDocumentConverter.isSupported("docx"));
    }

    @Test
    public void createFiles() throws IOException, DocumentException {
        createFile("gif", gif.getFile());
        createFile("tif", tif.getFile());
        createFile("png", png.getFile());
        createFile("jpg", jpg.getFile());
    }

    private void createFile(String ext, File file) throws IOException, DocumentException {
        FileInputStream inputStream = new FileInputStream(file);
        final byte[] convertedBytes = extendedDocumentConverter.convertToPdf(ext, inputStream);
        inputStream.close();
        FileOutputStream fos = new FileOutputStream("sample." + ext + ".pdf");
        fos.write(convertedBytes);
        fos.flush();
        fos.close();
    }
}
