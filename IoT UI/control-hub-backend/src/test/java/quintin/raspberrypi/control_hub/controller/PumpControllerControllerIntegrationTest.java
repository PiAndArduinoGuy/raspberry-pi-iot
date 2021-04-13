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
import quintin.raspberrypi.control_hub.channel.ControlHubChannels;
import quintin.raspberrypi.control_hub.domain.PumpState;
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
    private ControlHubChannels binding;


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
    @DisplayName("Given the ambient temp readings are 20.00 through 34.00 have been published to the ambienttemp queue" +
            "When the get /pump-controller/latest-average-ambient-temp-reading endpoint is hit" +
            "Then the response is 200 OK and the body contains the average of 27.00")
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
            "Then the response is a Zalando problem with 400 Bad request and detail says '15 ambient temperature readings have not yet been captured, an average could not be calculated'")
    @DirtiesContext
    void canGet400BadRequestResponseWithLessThan15AmbientTempReadings(){
        binding.newAmbientTempInput().send(MessageBuilder.withPayload(16.00).build());

        ResponseEntity<Problem> responseEntity =
                this.restTemplate.getForEntity("http://localhost:" + port +"control-hub-backend/pump-controller/latest-average-ambient-temp-reading", Problem.class);

        TestUtil.assertZalandoProblem(responseEntity.getBody(), HttpStatus.BAD_REQUEST,  "15 ambient temperature readings have not yet been captured, an average could not be calculated");
    }

    @Test
    @DisplayName("Given the ambient temp reading 10.00 has been published to ambienttemp queue" +
            " When get request is made to /pump-controller/latest-ambient-temp-reading" +
            " Then the response is 200 OK and the body is 10.00")
    @DirtiesContext
    void canGetOkResponseAndLatestAmbientTempFromPumpControllerWithOneAmbientTempReadingSent(){
        binding.newAmbientTempInput().send(MessageBuilder.withPayload(10.00).build());

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
        binding.newAmbientTempInput().send(MessageBuilder.withPayload(10.00).build());
        binding.newAmbientTempInput().send(MessageBuilder.withPayload(11.00).build());

        ResponseEntity<Double> responseEntity =
                this.restTemplate.getForEntity("http://localhost:" + port +"control-hub-backend/pump-controller/latest-ambient-temp-reading", Double.class);

        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(responseEntity.getBody()).isEqualTo(11.00);
    }

    @Test
    @DisplayName("Given no ambient temp reading has been sent" +
            " When the /pump-controller/latest-ambient-temp-reading endpoint is hit" +
            " Then the response is a Zalando problem with 400 Bad request and detail says 'An ambient temperature has not yet been sent.'")
    void canGetOkResponseAndLatestAvgAmbientTempReading(){
        ResponseEntity<Problem> responseEntity = restTemplate.getForEntity("http://localhost:" + port + "control-hub-backend/pump-controller/latest-ambient-temp-reading", Problem.class);

        TestUtil.assertZalandoProblem(responseEntity.getBody(), HttpStatus.BAD_REQUEST, "An ambient temperature has not yet been sent.");
    }


    private void assertPumpConfigValues(final PumpConfig pumpConfig, final double expectedTurnOffTemp, final OverrideStatus expectedOverrideStatus) {
        Assertions.assertThat(pumpConfig).isNotNull();
        Assertions.assertThat(pumpConfig.getTurnOffTemp()).isEqualTo(expectedTurnOffTemp);
        Assertions.assertThat(pumpConfig.getOverrideStatus()).isEqualTo(expectedOverrideStatus);
    }

    @Test
    @DisplayName("Given the pump controller state 'ON' has been published to the pumpcontrollertogglestatus queue" +
            " When the get /pump-controller/state endpoint is hit" +
            " Then the response is 200 OK with the pump state 'ON' in the body of the response")
    @DirtiesContext
    void canGetOkResponseForPumpControllerStateEndpointWithOnBody(){
        binding.pumpStateInput().send(MessageBuilder.withPayload("ON").build());

        ResponseEntity<PumpState> responseEntity = restTemplate.getForEntity("http://localhost:" + port + "control-hub-backend/pump-controller/state", PumpState.class);

        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(responseEntity.getBody()).isEqualTo(PumpState.ON);
    }

    @Test
    @DisplayName("Given the pump controller state 'OFF' has been published to the pumpcontrollertogglestatus queue" +
            " When the get /pump-controller/state endpoint is hit" +
            " Then the response is 200 OK with the PumpState.OFF in the body of the response")
    @DirtiesContext
    void canGetOkResponseForPumpControllerStateEndpointWithOffBody(){
        binding.pumpStateInput().send(MessageBuilder.withPayload("OFF").build());

        ResponseEntity<PumpState> responseEntity = restTemplate.getForEntity("http://localhost:" + port + "control-hub-backend/pump-controller/state", PumpState.class);

        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(responseEntity.getBody()).isEqualTo(PumpState.OFF);
    }

    @Test
    @DisplayName("Given the pump controller state 'OfF' has been published to the pumpcontrollertogglestatus queue" +
            " When the get /pump-controller/state endpoint is hit" +
            " Then the response is 500 Internal server error with the Zalando problem and detail says 'An invalid message has been published to the pumpcontrollertogglestatus queue. The message can only be one o 'ON' or 'OFF'")
    @DirtiesContext
    void canGetZalandoProblemResponseWithInvalidMessagePublished(){
        binding.pumpStateInput().send(MessageBuilder.withPayload("OfF").build());

        ResponseEntity<Problem> responseEntity = restTemplate.getForEntity("http://localhost:" + port + "control-hub-backend/pump-controller/state", Problem.class);

        TestUtil.assertZalandoProblem(responseEntity.getBody(), HttpStatus.INTERNAL_SERVER_ERROR, "An invalid message has been published to the pumpcontrollertogglestatus queue. The message can only be one o 'ON' or 'OFF'");
    }

    @Test
    @DisplayName("Given the pump controller state has not been published to the pumpcontrollertogglestatus" +
            " When the get /pump-controller/state is hit " +
            " Then the response is 400 Bad request with Zalando problem and details says 'The pump controller state has not been sent to the control hub. The state cannot be determined.'")
    void canGetZalandoProblemResponseWithNoPumpControllerStatePublished(){
        ResponseEntity<Problem> responseEntity = restTemplate.getForEntity("http://localhost:" + port + "control-hub-backend/pump-controller/state", Problem.class);

        TestUtil.assertZalandoProblem(responseEntity.getBody(), HttpStatus.BAD_REQUEST, "The pump controller state has not been sent to the control hub. The state cannot be determined.");
    }
}
