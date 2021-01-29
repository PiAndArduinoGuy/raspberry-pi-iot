package quintin.raspberrypi.pump_controller.subscriber;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.cloud.stream.messaging.Sink;
import org.springframework.cloud.stream.test.binder.MessageCollector;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.MessagingException;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.web.client.RestTemplate;
import quintin.raspberrypi.pump_controller.data.OverrideStatus;
import quintin.raspberrypi.pump_controller.data.Problem;
import quintin.raspberrypi.pump_controller.data.PumpConfig;
import quintin.raspberrypi.pump_controller.domain.AmbientTempReader;
import quintin.raspberrypi.pump_controller.domain.PumpToggler;
import quintin.raspberrypi.pump_controller.observable.PumpOverrideObservable;
import quintin.raspberrypi.pump_controller.observable.TurnOnTempObservable;
import quintin.raspberrypi.pump_controller.observer.AutomaticPumpToggler;
import quintin.raspberrypi.pump_controller.observer.ManualPumpToggler;
import quintin.raspberrypi.pump_controller.runner.PumpControllerInitializer;

import java.util.Observable;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringBootTest
class UpdatedPumpConfigUnitTest {

    @Autowired
    private Sink binding;

    @Autowired
    private PumpConfig pumpConfig;

    @Autowired
    private MessageCollector messageCollector;

    @Autowired
    private PumpOverrideObservable pumpOverrideObservable;

    @MockBean
    private AutomaticPumpToggler automaticPumpToggler;

    @MockBean
    private ManualPumpToggler manualPumpToggler;

    @Autowired
    private TurnOnTempObservable turnOnTempObservable;

    @MockBean
    private AmbientTempReader ambientTempReader;

    @MockBean
    private RestTemplate mockControlHubBaseRestTemplate;

    @MockBean
    private PumpControllerInitializer pumpControllerInitializer;

    @MockBean
    private PumpToggler pumpToggler;

    @SpyBean
    private UpdatedPumpConfig updatedPumpConfig;

    @Captor
    ArgumentCaptor<String> messageCaptor;

    @Test
    void givenPumpConfigurationMessageSent_whenPumpConfigUpdateMessageReceived_thenMessageMustBeSentToOutputPipe() throws Exception {
        // Given
        PumpConfig mockPumpConfigResponse = new PumpConfig();
        mockPumpConfigResponse.setOverrideStatus(OverrideStatus.NONE);
        mockPumpConfigResponse.setTurnOnTemp(20.00);
        when(mockControlHubBaseRestTemplate.getForEntity("/pump-configuration", Object.class))
                .thenReturn(new ResponseEntity<>(mockPumpConfigResponse, HttpStatus.OK));

        // When
        binding.input().send(MessageBuilder.withPayload("Pump configuration updated").build());

        // Then
        verify(updatedPumpConfig).sendPumpUpdateConfigToObservers(messageCaptor.capture());
        String receivedMessage = messageCaptor.getValue();
        assertThat(receivedMessage).isEqualToIgnoringCase("Pump configuration updated");
    }

    @Test
    void givenMessageSentNotRecognizedByPumpController_whenMessageReceived_thenExceptionThrownStatingMessageNotRecognized() {
        assertThatThrownBy(() -> {
            binding.input().send(MessageBuilder.withPayload("A message that should not be recognized").build());

        }).isInstanceOf(MessagingException.class)
                .hasMessageContaining("A message was received that is not recognized - Expected the message 'Pump configuration updated' but received 'A message that should not be recognized'");

    }

    @Test
    @DisplayName("Given the overrideStatus of the PumpConfig has changed" +
            "When the UpdatedPumpConfig class receives the update" +
            "Then it knows the OverrideStatus has changed and updates the PumpOverrideObservable and notifies the AutomaticPumpToggler and ManualPumpToggler observers of the change")
    void canNotifyOfUpdatedOverrideStatus() {
        // Given
        PumpConfig mockPumpConfigResponse = new PumpConfig();
        mockPumpConfigResponse.setOverrideStatus(OverrideStatus.NONE);
        when(mockControlHubBaseRestTemplate.getForEntity("/pump-configuration", Object.class))
                .thenReturn(new ResponseEntity<>(mockPumpConfigResponse, HttpStatus.OK));
        pumpOverrideObservable.addObserver(automaticPumpToggler);
        pumpOverrideObservable.addObserver(manualPumpToggler);

        // When
        binding.input().send(MessageBuilder.withPayload("Pump configuration updated").build());

        // Then
        verify(automaticPumpToggler).update(any(Observable.class), any(OverrideStatus.class));
        verify(manualPumpToggler).update(any(Observable.class), any(OverrideStatus.class));
    }

    @Test
    @DisplayName("Given the turnOnTemperature of the PumpConfig has changed" +
            "When the UpdatedPumpConfig class receives the update" +
            "Then it knows the turnOnTemperature has changed and updates the TurnOnTemperatureObservable class and notifies the AutomaticPumpToggler observers of the change")
    void canNotifyOfUpdatedTurnOnTemperature() {
        // Given
        PumpConfig mockPumpConfigResponse = new PumpConfig();
        mockPumpConfigResponse.setTurnOnTemp(20.00);
        when(mockControlHubBaseRestTemplate.getForEntity("/pump-configuration", Object.class))
                .thenReturn(new ResponseEntity<>(mockPumpConfigResponse, HttpStatus.OK));
        turnOnTempObservable.addObserver(automaticPumpToggler);

        // When
        binding.input().send(MessageBuilder.withPayload("Pump configuration updated").build());

        // Then
        verify(automaticPumpToggler).update(any(Observable.class), any(Double.class));
    }

    @Test
    @DisplayName("Given a status code other than 200 is received from the control hub" +
            "When an update to the PumpConfig is received" +
            "Then a PumpControllerException is thrown")
    void canThrowException() {

        // Given
        Problem zolandoProblem = new Problem(HttpStatus.BAD_REQUEST.getReasonPhrase(), HttpStatus.BAD_REQUEST.value(), "The request could not be serviced.");
        when(mockControlHubBaseRestTemplate.getForEntity("/pump-configuration", Object.class))
                .thenReturn(new ResponseEntity<>(zolandoProblem, HttpStatus.BAD_GATEWAY));

        // When and Then
        assertThatThrownBy(() -> {
            binding.input().send(MessageBuilder.withPayload("Pump configuration updated").build());
        }).isInstanceOf(MessagingException.class)
                .hasMessageContaining("The updated pump config could not be retrieved from the control hub, the response was: Problem(title=Bad Request, status=400, detail=The request could not be serviced.)");

    }
}