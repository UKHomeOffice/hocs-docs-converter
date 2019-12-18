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

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import static org.assertj.core.api.Java6Assertions.assertThat;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

@RunWith (SpringRunner.class)
@SpringBootTest (webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class DocumentConversionResourceTest {

    @Value ("classpath:testdata/sample.docx")
    private Resource docx;

    @Value ("classpath:testdata/sample.tif")
    private Resource tiff;

    @Value ("classpath:testdata/sample.tif.pdf")
    private Resource tiffPdf;

    @Value ("classpath:testdata/sample.jpg")
    private Resource jpg;

    @Value ("classpath:testdata/sample.doc")
    private Resource doc;

    @Value ("classpath:testdata/sample.pdf")
    private Resource pdf;

    @Value ("classpath:testdata/sample.qt")
    private Resource qt;

    @Value ("classpath:testdata/sample")
    private Resource none;

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

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

    }

    @Test
    public void shouldReturn200ForTIF() throws IOException {

        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Type", "multipart/form-data");
        MultiValueMap<String, Object> map = new LinkedMultiValueMap<>();
        map.set("file", new FileSystemResource(tiff.getFile()));

        ResponseEntity<String> response = restTemplate.exchange("/convert",
                HttpMethod.POST,
                new HttpEntity<>(map, headers),
                String.class);

        try (FileOutputStream fos = new FileOutputStream("./problem.pdf")) {
            fos.write(response.getBody().getBytes());
        }
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

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

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

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

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

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

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
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
        MultiValueMap<String, Object> map = new LinkedMultiValueMap<>();
        map.set("file", new FileSystemResource(none.getFile()));

        ResponseEntity<String> response = restTemplate.exchange("/convert",
                HttpMethod.POST,
                new HttpEntity<>(map, headers),
                String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);

    }

    @Test
    public void testOkExtDocConverterTiffAndContents() throws IOException {

        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Type", "multipart/form-data");
        MultiValueMap<String, Object> map = new LinkedMultiValueMap<>();
        map.set("file", new FileSystemResource(tiff.getFile()));

        ResponseEntity<byte[]> response = restTemplate.exchange("/convert",
                                                                HttpMethod.POST,
                                                                new HttpEntity<>(map, headers),
                                                                byte[].class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        final byte[] tiffBytes = IOUtils.toByteArray(new FileInputStream(tiffPdf.getFile()));
        assertEquals(tiffBytes.length, response.getBody().length);
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
        FileInputStream inputStream = new FileInputStream(tiff.getFile());
        final byte[] convertedBytes = extendedDocumentConverter.convertToPdf("tiff", inputStream);
        inputStream.close();
        final byte[] tiffBytes = IOUtils.toByteArray(new FileInputStream(tiffPdf.getFile()));
        assertEquals(tiffBytes.length, convertedBytes.length);
        // the contents cannot be compared as there are timestamps in the file
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
}
