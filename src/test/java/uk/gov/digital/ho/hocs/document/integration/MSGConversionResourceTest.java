package uk.gov.digital.ho.hocs.document.integration;

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

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@TestInstance(TestInstance.Lifecycle.PER_METHOD)
@SpringBootTest(webEnvironment = RANDOM_PORT)
public class MSGConversionResourceTest {

    @Value("classpath:testdata/sample1.msg")
    private Resource msg1;

    @Value("classpath:testdata/sample2.msg")
    private Resource msg2;

    @Value("classpath:testdata/sample3.msg")
    private Resource msg3;

    @Autowired
    private TestRestTemplate restTemplate;


    @Test
    public void testMsg1_Ok() throws IOException {

        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Type", "multipart/form-data");
        MultiValueMap<String, Object> map = new LinkedMultiValueMap<>();
        map.set("file", new FileSystemResource(msg1.getFile()));

        ResponseEntity<byte[]> response = restTemplate.exchange("/convert",
                HttpMethod.POST,
                new HttpEntity<>(map, headers),
                byte[].class);

        assertEquals(HttpStatus.OK, response.getStatusCode());

    }

    @Test
    public void testMsg2_Ok() throws IOException {

        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Type", "multipart/form-data");
        MultiValueMap<String, Object> map = new LinkedMultiValueMap<>();
        map.set("file", new FileSystemResource(msg2.getFile()));

        ResponseEntity<byte[]> response = restTemplate.exchange("/convert",
                HttpMethod.POST,
                new HttpEntity<>(map, headers),
                byte[].class);

        assertEquals(HttpStatus.OK, response.getStatusCode());

    }

    @Test
    public void testMsg3_Ok() throws IOException {

        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Type", "multipart/form-data");
        MultiValueMap<String, Object> map = new LinkedMultiValueMap<>();
        map.set("file", new FileSystemResource(msg3.getFile()));

        ResponseEntity<byte[]> response = restTemplate.exchange("/convert",
                HttpMethod.POST,
                new HttpEntity<>(map, headers),
                byte[].class);

        assertEquals(HttpStatus.OK, response.getStatusCode());

    }

}
