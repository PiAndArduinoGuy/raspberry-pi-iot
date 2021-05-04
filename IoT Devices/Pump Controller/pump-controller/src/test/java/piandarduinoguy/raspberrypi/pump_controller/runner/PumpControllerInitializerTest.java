package piandarduinoguy.raspberrypi.pump_controller.runner;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.client.RestTemplate;
import piandarduinoguy.raspberrypi.pump_controller.config.ControlHubRestTemplateConfig;
import piandarduinoguy.raspberrypi.pump_controller.data.PumpConfig;
import piandarduinoguy.raspberrypi.pump_controller.domain.PumpToggler;
import piandarduinoguy.raspberrypi.pump_controller.exception.PumpControllerException;
import piandarduinoguy.raspberrypi.pump_controller.observable.NewAmbientTempReadingObservable;
import piandarduinoguy.raspberrypi.pump_controller.observable.PumpOverrideStatusObservable;
import piandarduinoguy.raspberrypi.pump_controller.observable.PumpTurnOnTempObservable;
import piandarduinoguy.raspberrypi.pump_controller.observer.AmbientTempReader;
import piandarduinoguy.raspberrypi.pump_controller.observer.AutomaticPumpToggler;
import piandarduinoguy.raspberrypi.pump_controller.observer.ManualPumpToggler;
import piandarduinoguy.raspberrypi.pump_controller.publisher.AmbientTempPublisher;
import piandarduinoguy.raspberrypi.pump_controller.subscriber.UpdatedPumpConfigSubscriber;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {
        PumpControllerInitializer.class,
        PumpConfig.class,
        AutomaticPumpToggler.class,
        ManualPumpToggler.class,
        ControlHubRestTemplateConfig.class,
        AmbientTempReader.class,
        PumpToggler.class,
        PumpOverrideStatusObservable.class,
        PumpTurnOnTempObservable.class,
        NewAmbientTempReadingObservable.class,
        AmbientTempPublisher.class,
        UpdatedPumpConfigSubscriber.class
})
@TestPropertySource("classpath:application-test.properties")
class PumpControllerInitializerTest {
    @MockBean
    private AmbientTempPublisher ambientTempPublisher;

    @MockBean
    private PumpToggler pumpToggler;

    @MockBean
    private RestTemplate controlHubBaseRestTemplate;

    @MockBean
    private PumpOverrideStatusObservable pumpOverrideStatusObservableMock;

    @MockBean
    private PumpTurnOnTempObservable pumpTurnOnTempObservableMock;

    @Autowired
    private PumpControllerInitializer pumpControllerInitializer;

    @Test
    @DisplayName("Given the PumpConfig is obtained from the ControlHub" +
            "When the PumpControllerInitializer is run" +
            "Then the initial PumpConfig is broadcast to those needing the PumpConfig")
    void canSetInitialPumpConfig() {
        String mockPumpConfig = "{\"turnOnTemp\":20.0,\"overrideStatus\":\"NONE\"}";
        when(controlHubBaseRestTemplate.getForEntity("/pump-controller/pump-configuration", String.class))
                .thenReturn(new ResponseEntity(mockPumpConfig, HttpStatus.OK));

        pumpControllerInitializer.run();

        verify(pumpOverrideStatusObservableMock).notifyObservers(any());
        verify(pumpTurnOnTempObservableMock).notifyObservers(any());

    }

    @Test
    @DisplayName("Given the PumpConfig cannot be obtained due to some exception from the ControlHub" +
            "When the PumpControllerInitializer is run" +
            "Then the a PumpControllerException is thrown with a message showing the error response")
    void canThrowPumpControllerException() {
        String problemResponse = String.format("{\"title\": \"%s\", \"status\": \"%s\", \"detail\":\"%s\"}", HttpStatus.BAD_GATEWAY.getReasonPhrase(), HttpStatus.BAD_GATEWAY.value(), "Exception occurred in ControlHub");;
        when(controlHubBaseRestTemplate.getForEntity("/pump-controller/pump-configuration", String.class))
                .thenReturn(new ResponseEntity(problemResponse, HttpStatus.BAD_GATEWAY));

        assertThatThrownBy(() -> {
            pumpControllerInitializer.run();
        }).isInstanceOf(PumpControllerException.class)
                .hasMessage("The updated pump config could not be retrieved from the control hub, the response was a Problem - Problem(title=Bad Gateway, status=502, detail=Exception occurred in ControlHub).");

    }

    @Test
    @DisplayName("Given the PumpConfig cannot be obtained due to some exception from the ControlHub" +
            "When the PumpControllerInitializer is run" +
            "Then the a PumpControllerException is thrown with a message showing the error response")
    void canThrowPumpControllerExceptionIfJsonProcessingExceptionIsThrownForProblemMapping() {
        String problemResponse = String.format("{\"TITLE\": \"%s\", \"status\": \"%s\", \"detail\":\"%s\"}", HttpStatus.BAD_GATEWAY.getReasonPhrase(), HttpStatus.BAD_GATEWAY.value(), "Exception occurred in ControlHub");;
        when(controlHubBaseRestTemplate.getForEntity("/pump-controller/pump-configuration", String.class))
                .thenReturn(new ResponseEntity(problemResponse, HttpStatus.BAD_GATEWAY));

        assertThatThrownBy(() -> {
            pumpControllerInitializer.run();
        }).isInstanceOf(PumpControllerException.class)
                .hasMessage("Error occurred mapping Control Hub problem response.");

    }

    @Test
    @DisplayName("Given the PumpConfig cannot be obtained due to some exception from the ControlHub" +
            "When the PumpControllerInitializer is run" +
            "Then the a PumpControllerException is thrown with a message showing the error response")
    void canThrowPumpControllerExceptionIfJsonProcessingExceptionIsThrownForPumpConfigMapping() {
        String mockPumpConfig = "{\"turnOnTemp\":20.0,\"overrideStatus\":\"INVALID\"}";
        when(controlHubBaseRestTemplate.getForEntity("/pump-controller/pump-configuration", String.class))
                .thenReturn(new ResponseEntity(mockPumpConfig, HttpStatus.OK));

        assertThatThrownBy(() -> {
            pumpControllerInitializer.run();
        }).isInstanceOf(PumpControllerException.class)
                .hasMessage("Error occurred mapping Control Hub pump config response.");

    }

}