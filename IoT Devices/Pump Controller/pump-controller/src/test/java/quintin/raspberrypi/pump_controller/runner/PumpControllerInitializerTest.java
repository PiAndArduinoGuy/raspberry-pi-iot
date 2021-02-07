package quintin.raspberrypi.pump_controller.runner;

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
import quintin.raspberrypi.pump_controller.config.ControlHubRestTemplateConfig;
import quintin.raspberrypi.pump_controller.data.OverrideStatus;
import quintin.raspberrypi.pump_controller.data.Problem;
import quintin.raspberrypi.pump_controller.data.PumpConfig;
import quintin.raspberrypi.pump_controller.domain.AmbientTempReader;
import quintin.raspberrypi.pump_controller.domain.PumpToggler;
import quintin.raspberrypi.pump_controller.exception.PumpControllerException;
import quintin.raspberrypi.pump_controller.observable.NewAmbientTempReadingObservable;
import quintin.raspberrypi.pump_controller.observable.PumpOverrideStatusObservable;
import quintin.raspberrypi.pump_controller.observable.PumpTurnOnTempObservable;
import quintin.raspberrypi.pump_controller.observer.AutomaticPumpToggler;
import quintin.raspberrypi.pump_controller.observer.ManualPumpToggler;

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
        NewAmbientTempReadingObservable.class
})
@TestPropertySource("classpath:application-test.properties")
class PumpControllerInitializerTest {

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
        PumpConfig mockPumpConfig = new PumpConfig(20.00, OverrideStatus.PUMP_OFF);
        when(controlHubBaseRestTemplate.getForEntity("/pump-configuration", Object.class))
                .thenReturn(new ResponseEntity(mockPumpConfig, HttpStatus.OK));

        pumpControllerInitializer.run();

        verify(pumpOverrideStatusObservableMock).notifyObservers(any());
        verify(pumpTurnOnTempObservableMock).notifyObservers(any());

    }

    @Test
    @DisplayName("Given the PumpConfig cannot be obtained due to some exception from the ControlHub" +
            "When the PumpControllerInitializer is run" +
            "Then the a PumpControllerException is thrown with a message showing the error response")
    void canThrowPumpControllerException() throws Exception {
        Problem problemResponse = new Problem(HttpStatus.BAD_GATEWAY.getReasonPhrase(), HttpStatus.BAD_GATEWAY.value(), "Exception occurred in ControlHub");
        when(controlHubBaseRestTemplate.getForEntity("/pump-configuration", Object.class))
                .thenReturn(new ResponseEntity(problemResponse, HttpStatus.BAD_GATEWAY));

        assertThatThrownBy(() -> {
            pumpControllerInitializer.run();
        }).isInstanceOf(PumpControllerException.class)
                .hasMessage("The updated pump config could not be retrieved from the control hub, the response was: Problem(title=Bad Gateway, status=502, detail=Exception occurred in ControlHub)");

    }

}