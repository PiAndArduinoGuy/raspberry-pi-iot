package piandarduinoguy.raspberrypi.securitymsrv.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.TestPropertySource;
import piandarduinoguy.raspberrypi.securitymsrv.TestUtils;
import piandarduinoguy.raspberrypi.securitymsrv.domain.Problem;
import piandarduinoguy.raspberrypi.securitymsrv.domain.SecurityConfig;
import piandarduinoguy.raspberrypi.securitymsrv.domain.SecurityState;
import piandarduinoguy.raspberrypi.securitymsrv.domain.SecurityStatus;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource("classpath:application-test.properties")
class SecurityControllerSecurityConfigIntegrationTest {
    @Autowired
    private SecurityController securityController;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private TestUtils testUtils;

    private final SecurityConfig existingSecurityConfig = new SecurityConfig(SecurityStatus.BREACHED, SecurityState.DISARMED);

    @LocalServerPort
    private int port;

    @BeforeEach
    void setUp() throws Exception {
        // ensure there is a security config file to start off with
        testUtils.createSecurityConfigFile(existingSecurityConfig);
    }

    @DisplayName("Given a security config json object that is valid " +
            "when put endpoint hit to update security config " +
            "then 201 CREATED response returned and security config updated as expected.")
    @Test
    void canSaveUpdatedSecurityConfig() throws Exception {
        SecurityConfig updatedSecurityConfig = new SecurityConfig(SecurityStatus.SAFE, SecurityState.ARMED);
        HttpEntity<SecurityConfig> httpEntity = new HttpEntity<>(updatedSecurityConfig);
        ResponseEntity<Void> responseEntity = restTemplate.exchange("http://localhost:" + port + "/security/update/security-config", HttpMethod.PUT, httpEntity, Void.class);
        assertThat(responseEntity).isNotNull();
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        testUtils.assertThatExpectedSecurityConfigJsonFileSaved(updatedSecurityConfig);
    }

    @DisplayName("Given a security config json object exists " +
            "when get endpoint hit to " +
            "then 200 OK response security config returned.")
    @Test
    void canGetSecurityConfig() {
        ResponseEntity<SecurityConfig> responseEntity = restTemplate.getForEntity("http://localhost:" + port + "/security/security-config", SecurityConfig.class);
        assertThat(responseEntity).isNotNull();
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
        SecurityConfig returnedSecurityConfig = responseEntity.getBody();
        assertThat(returnedSecurityConfig).isNotNull();
        assertThat(returnedSecurityConfig.getSecurityStatus()).isEqualTo(existingSecurityConfig.getSecurityStatus());
        assertThat(returnedSecurityConfig.getSecurityState()).isEqualTo(existingSecurityConfig.getSecurityState());
    }

    @DisplayName("Given an annotated image exists " +
            "when get to the /annotated-image endpoint is made " +
            "then a base64 encoded image is returned with status 200 OK.")
    @Test
    void canGetAnnotatedImage() throws Exception {
        testUtils.createExpectedAnnotatedImageFile();

        ResponseEntity<String> responseEntity = restTemplate.getForEntity("http://localhost:" + port + "/security/annotated-image", String.class);

        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(responseEntity.getBody()).isEqualToIgnoringCase(testUtils.getExpectedBase64EncodedAnnotatedImage());

        testUtils.deleteAnnotatedImage();
    }

    @DisplayName("Given an annotated image does not exist " +
            "when get to the /annotated-image endpoint is made " +
            "then return expected Zalando problem.")
    @Test
    void canReturnZalandoProblemIfNoAnnotatedImageExists() {
        ResponseEntity<Problem> responseEntity = restTemplate.getForEntity("http://localhost:" + port + "/security/annotated-image", Problem.class);

        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        Problem problem = responseEntity.getBody();
        assertThat(problem).isNotNull();
        assertThat(problem.getStatus()).isEqualTo(HttpStatus.NOT_FOUND.value());
        assertThat(problem.getDetail()).isEqualToIgnoringCase("The File src/test/resources/application/test_new_capture_annotated.jpeg does not exist.");
        assertThat(problem.getTitle()).isEqualToIgnoringCase(HttpStatus.NOT_FOUND.getReasonPhrase());
    }

    @DisplayName("Given an IOException thrown " +
            "when get to the /annotated-image endpoint is made " +
            "then return expected Zalando problem")
    @Test
    @Disabled("Requires mocking of static Base64.encode method, use powermock for this then remove this annotation.")
    void canReturnZalandoProblemIfGetAnnotatedImageMethodThrowsIoException() {
        ResponseEntity<Problem> responseEntity = restTemplate.getForEntity("http://localhost:" + port + "/security/annotated-image", Problem.class);

        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        Problem problem = responseEntity.getBody();
        assertThat(problem).isNotNull();
        assertThat(problem.getStatus()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR.value());
        assertThat(problem.getDetail()).isEqualToIgnoringCase("The image test_new_capture_annotated.jpeg could not be encoded to base64 string due to an IOException being thrown with message \"I am an IOException\".");
        assertThat(problem.getTitle()).isEqualToIgnoringCase(HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase());
    }

    @DisplayName("Given an IOException thrown " +
            "when post to the /object-detect endpoint is made " +
            "then return expected Zalando problem")
    @Test
    @Disabled("Requires mocking of static Base64.encode method, use powermock for this then remove this annotation.")
    void canReturnZalandoProblemIfSaveImageMethodThrowsIoException() {
        ResponseEntity<Problem> responseEntity = restTemplate.getForEntity("http://localhost:" + port + "/security/object-detect", Problem.class);

        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        Problem problem = responseEntity.getBody();
        assertThat(problem).isNotNull();
        assertThat(problem.getStatus()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR.value());
        assertThat(problem.getDetail()).isEqualToIgnoringCase("The image %s could not be saved to the directory %s. An IOException was thrown with message \"I am an IOException\".");
        assertThat(problem.getTitle()).isEqualToIgnoringCase(HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase());
    }


    @AfterEach
    void tearDown() {
        testUtils.deleteSecurityConfigFile();
    }
}
