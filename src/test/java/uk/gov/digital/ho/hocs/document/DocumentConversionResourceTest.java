package uk.gov.digital.ho.hocs.document;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.*;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import java.io.IOException;
import java.net.URISyntaxException;

import static org.assertj.core.api.Java6Assertions.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class DocumentConversionResourceTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    public void shouldReturn200ForValidFileUpload() throws IOException, URISyntaxException {

        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Type", "multipart/form-data");
        MultiValueMap<String, Object> map = new LinkedMultiValueMap<String, Object>();
        map.set("file", new FileSystemResource("testdata/sample.docx"));

        ResponseEntity<String> response = restTemplate.exchange("/uploadFile",
                HttpMethod.POST,
                new HttpEntity<>(map, headers),
                String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

    }

    @Test
    public void shouldReturn200ForTIF() throws IOException, URISyntaxException {

        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Type", "multipart/form-data");
        MultiValueMap<String, Object> map = new LinkedMultiValueMap<String, Object>();
        map.set("file", new FileSystemResource("testdata/sample.tif"));

        ResponseEntity<String> response = restTemplate.exchange("/uploadFile",
                HttpMethod.POST,
                new HttpEntity<>(map, headers),
                String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

    }

    @Test
    public void shouldReturn200ForJPEG() throws IOException, URISyntaxException {

        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Type", "multipart/form-data");
        MultiValueMap<String, Object> map = new LinkedMultiValueMap<String, Object>();
        map.set("file", new FileSystemResource("testdata/sample.jpg"));

        ResponseEntity<String> response = restTemplate.exchange("/uploadFile",
                HttpMethod.POST,
                new HttpEntity<>(map, headers),
                String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

    }

    @Test
    public void shouldReturn200ForDOC() throws IOException, URISyntaxException {

        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Type", "multipart/form-data");
        MultiValueMap<String, Object> map = new LinkedMultiValueMap<String, Object>();
        map.set("file", new FileSystemResource("testdata/sample.doc"));

        ResponseEntity<String> response = restTemplate.exchange("/uploadFile",
                HttpMethod.POST,
                new HttpEntity<>(map, headers),
                String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

    }

    @Test
    public void shouldReturn200ForDOCX() throws IOException, URISyntaxException {

        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Type", "multipart/form-data");
        MultiValueMap<String, Object> map = new LinkedMultiValueMap<String, Object>();
        map.set("file", new FileSystemResource("testdata/sample.docx"));

        ResponseEntity<String> response = restTemplate.exchange("/uploadFile",
                HttpMethod.POST,
                new HttpEntity<>(map, headers),
                String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

    }

    @Test
    public void shouldReturn200ForPDF() throws IOException, URISyntaxException {

        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Type", "multipart/form-data");
        MultiValueMap<String, Object> map = new LinkedMultiValueMap<String, Object>();
        map.set("file", new FileSystemResource("testdata/sample.pdf"));

        ResponseEntity<String> response = restTemplate.exchange("/uploadFile",
                HttpMethod.POST,
                new HttpEntity<>(map, headers),
                String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

    }

    @Test
    public void shouldReturn500ForBadFileExtensions() throws IOException, URISyntaxException {

        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Type", "multipart/form-data");
        MultiValueMap<String, Object> map = new LinkedMultiValueMap<String, Object>();
        map.set("file", new FileSystemResource("testdata/sample.qt"));

        ResponseEntity<String> response = restTemplate.exchange("/uploadFile",
                HttpMethod.POST,
                new HttpEntity<>(map, headers),
                String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);

    }


}