package quintin.raspberrypi.control_hub.controller;

import java.io.File;
import java.io.IOException;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.util.ResourceUtils;
import quintin.raspberrypi.control_hub.OverrideStatus;
import quintin.raspberrypi.control_hub.PumpConfig;
import quintin.raspberrypi.control_hub.exception.Problem;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource("classpath:application-test.properties")
class RaspberryPiControllerIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @LocalServerPort
    private int port;

    @Value("${pump-config-file-location}")
    private String pumpConfigFileLocation;

    @Test
    @DirtiesContext
    void canGetOkResponseForCreatingAValidNewPumpConfigInstance(){
        ResponseEntity<Void> responseEntity =
                this.restTemplate.postForEntity(
                        "http://localhost:"+port+"/control-hub-backend/pump-configuration/new",
                        new PumpConfig(20.00, OverrideStatus.NONE),
                        Void.class);

        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    @DirtiesContext
    void canGetZalandoProblemResponseForInvalidPumpConfigInstance(){
        ResponseEntity<Problem> responseEntity =
                this.restTemplate.postForEntity(
                        "http://localhost:"+port+"/control-hub-backend/pump-configuration/new",
                        new PumpConfig(51.00, OverrideStatus.NONE),
                        Problem.class);

        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertZalandoProblem(responseEntity.getBody(), HttpStatus.BAD_REQUEST, "The specified turn off temperature cannot be more than 50 degrees celsius");
    }

    @Test
    @DirtiesContext
    void canGetOkResponseAndExpectedPumpConfigBodyWhenHittingPumpConfigurationGetEndpoint(){
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            objectMapper.writeValue(new File(pumpConfigFileLocation), new PumpConfig(25.00, OverrideStatus.PUMP_ON));
        } catch (IOException e) {
            fail("An IOException was thrown while preparing the test: ", e);
        }

        ResponseEntity<PumpConfig> responseEntity =
                this.restTemplate.getForEntity(
                        "http://localhost:"+port+"/control-hub-backend/pump-configuration",
                        PumpConfig.class);

        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertPumpConfigValues(responseEntity.getBody(), 25.00, OverrideStatus.PUMP_ON);
    }

    private void assertPumpConfigValues(final PumpConfig pumpConfig, final double expectedTurnOffTemp, final OverrideStatus expectedOverrideStatus) {
        assertThat(pumpConfig).isNotNull();
        assertThat(pumpConfig.getTurnOffTemp()).isEqualTo(expectedTurnOffTemp);
        assertThat(pumpConfig.getOverrideStatus()).isEqualTo(expectedOverrideStatus);
    }

    private void assertZalandoProblem(final Problem zalandoProblem, final HttpStatus httpStatus, final String detail) {
        assertThat(zalandoProblem.getTitle()).isEqualToIgnoringCase(httpStatus.getReasonPhrase());
        assertThat(zalandoProblem.getDetail()).isEqualToIgnoringCase(detail);
        assertThat(zalandoProblem.getStatus()).isEqualTo(httpStatus.value());
    }

}
