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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class MSGConversionResourceTest {

    @Value("classpath:testdata/sample1.msg")
    private Resource msg1;

    @Value("classpath:testdata/sample2.msg")
    private Resource msg2;

    @Value("classpath:testdata/sample3.msg")
    private Resource msg3;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private MSGDocumentConverter msgDocumentConverter;

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

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

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

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

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

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

    }


    @Test
    public void checkExtendedSupport() {
        assertTrue(msgDocumentConverter.isSupported("msg"));

        assertFalse(msgDocumentConverter.isSupported("jpg"));
        assertFalse(msgDocumentConverter.isSupported("png"));
        assertFalse(msgDocumentConverter.isSupported("tif"));
        assertFalse(msgDocumentConverter.isSupported("gif"));
        assertFalse(msgDocumentConverter.isSupported("jpeg"));
        assertFalse(msgDocumentConverter.isSupported("tiff"));
        assertFalse(msgDocumentConverter.isSupported("doc"));
        assertFalse(msgDocumentConverter.isSupported("rtf"));
        assertFalse(msgDocumentConverter.isSupported("txt"));
        assertFalse(msgDocumentConverter.isSupported("docx"));
    }

}
