package uk.gov.digital.ho.hocs.document;

import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.pdf.PdfWriter;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.*;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import static org.assertj.core.api.Java6Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@TestInstance(TestInstance.Lifecycle.PER_METHOD)
@SpringBootTest(webEnvironment = RANDOM_PORT)
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
    private ImageDocumentConverter imageDocumentConverter;

    @Test
    public void shouldReturn200ForValidFileUpload() throws IOException {

        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Type", "multipart/form-data");
        MultiValueMap<String, Object> map = new LinkedMultiValueMap<String, Object>();
        map.set("file", new FileSystemResource(docx.getFile()));

        ResponseEntity<String> response = restTemplate.exchange("/convert",
                HttpMethod.POST,
                new HttpEntity<>(map, headers),
                String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

    }

    @Test
    public void shouldReturn200ForTIF() throws IOException {

        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Type", "multipart/form-data");
        MultiValueMap<String, Object> map = new LinkedMultiValueMap<String, Object>();
        map.set("file", new FileSystemResource(tif.getFile()));

        ResponseEntity<String> response = restTemplate.exchange("/convert",
                HttpMethod.POST,
                new HttpEntity<>(map, headers),
                String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

    }

    @Test
    public void shouldReturn200ForJPEG() throws IOException {

        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Type", "multipart/form-data");
        MultiValueMap<String, Object> map = new LinkedMultiValueMap<String, Object>();
        map.set("file", new FileSystemResource(jpg.getFile()));

        ResponseEntity<String> response = restTemplate.exchange("/convert",
                HttpMethod.POST,
                new HttpEntity<>(map, headers),
                String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

    }

    @Test
    public void shouldReturn200ForDOC() throws IOException {

        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Type", "multipart/form-data");
        MultiValueMap<String, Object> map = new LinkedMultiValueMap<String, Object>();
        map.set("file", new FileSystemResource(doc.getFile()));

        ResponseEntity<String> response = restTemplate.exchange("/convert",
                HttpMethod.POST,
                new HttpEntity<>(map, headers),
                String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

    }

    @Test
    public void shouldReturn200ForDOCX() throws IOException {

        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Type", "multipart/form-data");
        MultiValueMap<String, Object> map = new LinkedMultiValueMap<String, Object>();
        map.set("file", new FileSystemResource(docx.getFile()));

        ResponseEntity<String> response = restTemplate.exchange("/convert",
                HttpMethod.POST,
                new HttpEntity<>(map, headers),
                String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    public void shouldReturn200ForPDF() throws IOException {

        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Type", "multipart/form-data");
        MultiValueMap<String, Object> map = new LinkedMultiValueMap<String, Object>();
        map.set("file", new FileSystemResource(pdf.getFile()));

        ResponseEntity<String> response = restTemplate.exchange("/convert",
                HttpMethod.POST,
                new HttpEntity<>(map, headers),
                String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

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

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);

    }

    @Test
    public void shouldReturn400ForEmptyFileExtensions() throws IOException {

        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Type", "multipart/form-data");
        MultiValueMap<String, Object> map = new LinkedMultiValueMap<String, Object>();
        map.set("file", new FileSystemResource(none.getFile()));

        ResponseEntity<String> response = restTemplate.exchange("/convert",
                HttpMethod.POST,
                new HttpEntity<>(map, headers),
                String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);

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

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        final byte[] bytes = IOUtils.toByteArray(new FileInputStream(tifPdf.getFile()));
        assertEquals(bytes.length, response.getBody().length);
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

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        final byte[] bytes = IOUtils.toByteArray(new FileInputStream(gifPdf.getFile()));
        assertEquals(bytes.length, response.getBody().length);
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

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        final byte[] bytes = IOUtils.toByteArray(new FileInputStream(jpgPdf.getFile()));
        assertEquals(bytes.length, response.getBody().length);
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

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        final byte[] bytes = IOUtils.toByteArray(new FileInputStream(pngPdf.getFile()));
        assertEquals(bytes.length, response.getBody().length);
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

        assertThat(converted.getStatusCode()).isEqualTo(HttpStatus.OK);

        final byte[] originalBytes = FileUtils.readFileToByteArray(pdf.getFile());
        assertArrayEquals(originalBytes, converted.getBody());
    }

    @Test
    public void textExtendedDocumentConverterTiffDirectly() throws IOException, DocumentException {
        FileInputStream inputStream = new FileInputStream(tif.getFile());

        Document pdf = new Document();
        final ByteArrayOutputStream arrayOutputStream = new ByteArrayOutputStream();
        PdfWriter.getInstance(pdf, arrayOutputStream);
        pdf.open();

        imageDocumentConverter.convertToPdf(pdf,"tif", inputStream);

        pdf.close();
        inputStream.close();
        //assertEquals(2511535, convertedBytes);
    }

    @Test
    public void checkExtendedSupport() {
        assertTrue(imageDocumentConverter.isSupported("jpg"));
        assertTrue(imageDocumentConverter.isSupported("png"));
        assertTrue(imageDocumentConverter.isSupported("tif"));
        assertTrue(imageDocumentConverter.isSupported("gif"));
        assertTrue(imageDocumentConverter.isSupported("jpeg"));
        assertTrue(imageDocumentConverter.isSupported("tiff"));

        assertFalse(imageDocumentConverter.isSupported("doc"));
        assertFalse(imageDocumentConverter.isSupported("rtf"));
        assertFalse(imageDocumentConverter.isSupported("txt"));
        assertFalse(imageDocumentConverter.isSupported("docx"));
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

        Document pdf = new Document();
        final ByteArrayOutputStream arrayOutputStream = new ByteArrayOutputStream();
        PdfWriter.getInstance(pdf, arrayOutputStream);
        pdf.open();

        imageDocumentConverter.convertToPdf(pdf, ext, inputStream);

        pdf.close();

        inputStream.close();

        FileOutputStream fos = new FileOutputStream("sample." + ext + ".pdf");
        fos.write(arrayOutputStream.toByteArray());
        fos.flush();
        fos.close();
    }
}
