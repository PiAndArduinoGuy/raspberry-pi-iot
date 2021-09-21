package piandarduinoguy.raspberrypi.securitymsrv.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.ResourceUtils;
import org.springframework.web.multipart.MultipartFile;
import piandarduinoguy.raspberrypi.securitymsrv.TestUtils;
import piandarduinoguy.raspberrypi.securitymsrv.domain.SecurityConfig;
import piandarduinoguy.raspberrypi.securitymsrv.domain.SecurityState;
import piandarduinoguy.raspberrypi.securitymsrv.domain.SecurityStatus;
import piandarduinoguy.raspberrypi.securitymsrv.service.SecurityConfigService;

import java.io.File;
import java.io.FileInputStream;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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

    @AfterEach
    void tearDown() {
        testUtils.deleteSecurityConfigFile();
    }
}
