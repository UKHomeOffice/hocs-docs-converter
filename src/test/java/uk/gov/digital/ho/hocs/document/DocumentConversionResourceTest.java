package uk.gov.digital.ho.hocs.document;

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

import java.io.IOException;

import static org.assertj.core.api.Java6Assertions.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class DocumentConversionResourceTest {

    @Value("classpath:testdata/sample.docx")
    private Resource docx;

    @Value("classpath:testdata/sample.tif")
    private Resource tiff;

    @Value("classpath:testdata/sample.jpg")
    private Resource jpg;

    @Value("classpath:testdata/sample.doc")
    private Resource doc;

    @Value("classpath:testdata/sample.pdf")
    private Resource pdf;

    @Value("classpath:testdata/sample.qt")
    private Resource qt;

    @Value("classpath:testdata/sample")
    private Resource none;

    @Autowired
    private TestRestTemplate restTemplate;

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
        map.set("file", new FileSystemResource(tiff.getFile()));

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
        MultiValueMap<String, Object> map = new LinkedMultiValueMap<String, Object>();
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

}