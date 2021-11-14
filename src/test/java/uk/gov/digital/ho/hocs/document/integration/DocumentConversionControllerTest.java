package uk.gov.digital.ho.hocs.document.integration;

import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@TestInstance(TestInstance.Lifecycle.PER_METHOD)
@SpringBootTest(webEnvironment = RANDOM_PORT)
public class DocumentConversionControllerTest {

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
        assertEquals(bytes.length, Objects.requireNonNull(response.getBody()).length);
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
        assertEquals(bytes.length, Objects.requireNonNull(response.getBody()).length);
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
        assertEquals(bytes.length,                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                     Objects.requireNonNull(response.getBody()).length);
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
        assertEquals(bytes.length, Objects.requireNonNull(response.getBody()).length);
    }
}
