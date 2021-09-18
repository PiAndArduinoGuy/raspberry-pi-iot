package piandarduinoguy.raspberrypi.securitymsrv.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
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
import piandarduinoguy.raspberrypi.securitymsrv.domain.SecurityConfig;
import piandarduinoguy.raspberrypi.securitymsrv.domain.SecurityState;
import piandarduinoguy.raspberrypi.securitymsrv.domain.SecurityStatus;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource("classpath:application-test.properties")
class SecurityControllerIntegrationTest {
    @Autowired
    private SecurityController securityController;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private TestUtils testUtils;

    @LocalServerPort
    private int port;

    @DisplayName("Given a security config json object that is valid " +
            "when put endpoint hit to update security config " +
            "then 201 CREATED response returned and security config updated as expected.")
    @Test
    void canSaveUpdatedSecurityConfig() throws Exception{
        // ensure there is a security config file to be updated
        testUtils.createSecurityConfigFile(new SecurityConfig(SecurityStatus.BREACHED, SecurityState.DISARMED));

        SecurityConfig updatedSecurityConfig = new SecurityConfig(SecurityStatus.SAFE, SecurityState.ARMED);
        HttpEntity<SecurityConfig> httpEntity = new HttpEntity<>(updatedSecurityConfig);
        ResponseEntity<Void> responseEntity = restTemplate.exchange("http://localhost:"+port+"/security/update/security-config", HttpMethod.PUT, httpEntity, Void.class);
        assertThat(responseEntity).isNotNull();
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        testUtils.assertThatExpectedSecurityConfigJsonFileSaved(updatedSecurityConfig);

        testUtils.deleteSecurityConfigFile();
    }
}
