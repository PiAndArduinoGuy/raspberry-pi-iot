package quintin.raspberrypi.control_hub.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.cloud.stream.messaging.Sink;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.util.ResourceUtils;
import quintin.raspberrypi.control_hub.OverrideStatus;
import quintin.raspberrypi.control_hub.PumpConfig;
import quintin.raspberrypi.control_hub.exception.Problem;
import quintin.raspberrypi.control_hub.util.TestUtil;

import java.io.IOException;

import static org.assertj.core.api.Assertions.fail;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class PumpControllerControllerIntegrationTest {

    @LocalServerPort
    int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private Sink binding;

    @Test
    @DisplayName("Given the ambient temp readings are 15.00 through 30.00 have been published to the ambienttemp queue and the ambient temp readings list is of size 15" +
            "When the get /pump-controller/latest-average-ambient-temp-reading endpoint is hit" +
            "Then the response is 200 OK and the body contains the average of 22.50")
    @DirtiesContext
    void canGetLatestAverageTemp200OkResponse(){
        TestUtil.sendFifteenAmbientTempReadingsUsingBinding(binding);

        ResponseEntity<Double> responseEntity =
                this.restTemplate.getForEntity("http://localhost:" + port +"control-hub-backend/pump-controller/latest-average-ambient-temp-reading", Double.class);

        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(responseEntity.getBody()).isEqualTo(27.00);
    }

    @Test
    @DisplayName("Given the ambient temp reading 15.00 has been published to the ambienttemp queue and the ambient remp readings list has a size less than 15" +
            "When the get /pump-controller/latest-average-ambient-temp-reading endpoint is hit" +
            "Then the response is a Zalando problem with 400 Bad request and detail says '15 ambient temperature readings have not yet been captured.'")
    @DirtiesContext
    void canGet400BadRequestResponseWithLessThan15AmbientTempReadings(){
        binding.input().send(MessageBuilder.withPayload(16.00).build());

        ResponseEntity<Problem> responseEntity =
                this.restTemplate.getForEntity("http://localhost:" + port +"control-hub-backend/pump-controller/latest-average-ambient-temp-reading", Problem.class);

        Problem expectedZalandoProblem = new Problem(HttpStatus.BAD_REQUEST.getReasonPhrase(), HttpStatus.BAD_REQUEST.value(), "15 ambient temperature readings have not yet been captured.");


        TestUtil.assertZalandoProblem(expectedZalandoProblem, responseEntity.getStatusCode(),  "15 ambient temperature readings have not yet been captured.");
    }

    @Test
    void canGetOkResponseForCreatingAValidNewPumpConfigInstance(){
        ResponseEntity<Void> responseEntity =
                this.restTemplate.postForEntity(
                        "http://localhost:"+port+"/control-hub-backend/pump-controller/pump-configuration/new",
                        new PumpConfig(20.00, OverrideStatus.NONE),
                        Void.class);

        Assertions.assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void canGetZalandoProblemResponseForInvalidPumpConfigInstance(){
        ResponseEntity<Problem> responseEntity =
                this.restTemplate.postForEntity(
                        "http://localhost:"+port+"/control-hub-backend/pump-controller/pump-configuration/new",
                        new PumpConfig(51.00, OverrideStatus.NONE),
                        Problem.class);

        Assertions.assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        TestUtil.assertZalandoProblem(responseEntity.getBody(), HttpStatus.BAD_REQUEST, "The specified turn off temperature cannot be more than 50 degrees celsius");
    }

    @Test
    void canGetOkResponseAndExpectedPumpConfigBodyWhenHittingPumpConfigurationGetEndpoint(){
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            objectMapper.writeValue(ResourceUtils.getFile("classpath:pump/pump_config.json"), new PumpConfig(25.00, OverrideStatus.PUMP_ON));
        } catch (IOException e) {
            fail("An IOException was thrown while preparing the test: ", e);
        }

        ResponseEntity<PumpConfig> responseEntity =
                this.restTemplate.getForEntity(
                        "http://localhost:"+port+"control-hub-backend/pump-controller/pump-configuration",
                        PumpConfig.class);

        Assertions.assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertPumpConfigValues(responseEntity.getBody(), 25.00, OverrideStatus.PUMP_ON);
    }

    @Test
    @DisplayName("Given the ambient temp reading 10.00 has been published to ambienttemp queue" +
            " When get request is made to /pump-controller/latest-ambient-temp-reading" +
            " Then the response is 200 OK and the body is 10.00")
    @DirtiesContext
    void canGetOkResponseAndLatestAmbientTempFromPumpControllerWithOneAmbientTempReadingSent(){
        binding.input().send(MessageBuilder.withPayload(10.00).build());

        ResponseEntity<Double> responseEntity =
                this.restTemplate.getForEntity("http://localhost:" + port +"control-hub-backend/pump-controller/latest-ambient-temp-reading", Double.class);

        Assertions.assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
        Assertions.assertThat(responseEntity.getBody()).isEqualTo(10.00);
    }

    @Test
    @DisplayName("Given the ambient temp reading 10.00 and 11.00 has been published to ambienttemp queue" +
            " When get request is made to /pump-controller/latest-ambient-temp-reading" +
            " Then the response is 200 OK and the body is 11.00")
    @DirtiesContext
    void canGetOkResponseAndLatestAmbientTempFromPumpControllerWithMultipleAmbientTempReadingsSend(){
        binding.input().send(MessageBuilder.withPayload(10.00).build());
        binding.input().send(MessageBuilder.withPayload(11.00).build());

        ResponseEntity<Double> responseEntity =
                this.restTemplate.getForEntity("http://localhost:" + port +"control-hub-backend/pump-controller/latest-ambient-temp-reading", Double.class);

        Assertions.assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
        Assertions.assertThat(responseEntity.getBody()).isEqualTo(11.00);
    }



    private void assertPumpConfigValues(final PumpConfig pumpConfig, final double expectedTurnOffTemp, final OverrideStatus expectedOverrideStatus) {
        Assertions.assertThat(pumpConfig).isNotNull();
        Assertions.assertThat(pumpConfig.getTurnOffTemp()).isEqualTo(expectedTurnOffTemp);
        Assertions.assertThat(pumpConfig.getOverrideStatus()).isEqualTo(expectedOverrideStatus);
    }
}
