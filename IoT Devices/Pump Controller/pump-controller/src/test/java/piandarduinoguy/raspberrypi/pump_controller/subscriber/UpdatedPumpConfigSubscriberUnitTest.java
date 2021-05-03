package piandarduinoguy.raspberrypi.pump_controller.subscriber;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.cloud.stream.test.binder.MessageCollector;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.web.client.RestTemplate;
import piandarduinoguy.raspberrypi.pump_controller.channel.PumpControllerChannels;
import piandarduinoguy.raspberrypi.pump_controller.data.OverrideStatus;
import piandarduinoguy.raspberrypi.pump_controller.data.PumpConfig;
import piandarduinoguy.raspberrypi.pump_controller.domain.PumpToggler;
import piandarduinoguy.raspberrypi.pump_controller.observable.PumpOverrideStatusObservable;
import piandarduinoguy.raspberrypi.pump_controller.observable.PumpTurnOnTempObservable;
import piandarduinoguy.raspberrypi.pump_controller.observer.AmbientTempReader;
import piandarduinoguy.raspberrypi.pump_controller.observer.AutomaticPumpToggler;
import piandarduinoguy.raspberrypi.pump_controller.observer.ManualPumpToggler;
import piandarduinoguy.raspberrypi.pump_controller.runner.PumpControllerInitializer;

import java.util.Observable;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;

@SpringBootTest
class UpdatedPumpConfigSubscriberUnitTest {

    @Autowired
    private PumpControllerChannels binding;

    @Autowired
    private PumpConfig pumpConfig;

    @Autowired
    private MessageCollector messageCollector;

    @Autowired
    private PumpOverrideStatusObservable pumpOverrideStatusObservable;

    @MockBean
    private AutomaticPumpToggler automaticPumpToggler;

    @MockBean
    private ManualPumpToggler manualPumpToggler;

    @Autowired
    private PumpTurnOnTempObservable pumpTurnOnTempObservable;

    @MockBean
    private AmbientTempReader ambientTempReader;

    @MockBean
    private RestTemplate mockControlHubBaseRestTemplate;

    @MockBean
    private PumpControllerInitializer pumpControllerInitializer;

    @MockBean
    private PumpToggler pumpToggler;

    @SpyBean
    private UpdatedPumpConfigSubscriber updatedPumpConfigSubscriber;

    @Captor
    ArgumentCaptor<PumpConfig> messageCaptor;

    @Test
    void givenPumpConfigurationMessageSent_whenPumpConfigUpdateMessageReceived_thenMessageMustBeSentToOutputPipe() throws Exception {
        // Given
        PumpConfig initialPumpConfig = new PumpConfig(10.00, OverrideStatus.NONE);
        updatedPumpConfigSubscriber.setInitialPumpConfig(initialPumpConfig);
        PumpConfig newPumpConfig = new PumpConfig();
        newPumpConfig.setOverrideStatus(OverrideStatus.NONE);
        newPumpConfig.setTurnOnTemp(20.00);

        // When
        binding.updatedPumpConfigInput().send(MessageBuilder.withPayload(newPumpConfig).build());

        // Then
        verify(updatedPumpConfigSubscriber).sendPumpUpdateConfigToObservers(messageCaptor.capture());
        PumpConfig receivedMessage = messageCaptor.getValue();
        assertThat(receivedMessage.getOverrideStatus()).isEqualTo(newPumpConfig.getOverrideStatus());
        assertThat(receivedMessage.getTurnOnTemp()).isEqualTo(newPumpConfig.getTurnOnTemp());
    }


    @Test
    @DisplayName("Given the overrideStatus of the PumpConfig has changed and the automaticPumpToggler, manualPumpToggler and ambientTempReader classes are observers" +
            "When the UpdatedPumpConfig class receives the update" +
            "Then it knows the OverrideStatus has changed and updates the PumpOverrideObservable and notifies the AutomaticPumpToggler, AmbientTempReader and ManualPumpToggler observers of the change")
    void canNotifyOfUpdatedOverrideStatus() {
        // Given
        PumpConfig oldPumpConfig = new PumpConfig(10.00, OverrideStatus.PUMP_OFF);
        updatedPumpConfigSubscriber.setInitialPumpConfig(oldPumpConfig);
        updatedPumpConfigSubscriber.setInitialPumpConfig(oldPumpConfig);
        PumpConfig newPumpConfig = new PumpConfig();
        newPumpConfig.setOverrideStatus(OverrideStatus.NONE);
        newPumpConfig.setTurnOnTemp(10.00);
        pumpOverrideStatusObservable.addObserver(automaticPumpToggler);
        pumpOverrideStatusObservable.addObserver(manualPumpToggler);
        pumpOverrideStatusObservable.addObserver(ambientTempReader);

        // When
        binding.updatedPumpConfigInput().send(MessageBuilder.withPayload(newPumpConfig).build());

        // Then
        verify(automaticPumpToggler).update(any(Observable.class), any(OverrideStatus.class));
        verify(manualPumpToggler).update(any(Observable.class), any(OverrideStatus.class));
        verify(ambientTempReader).update(any(Observable.class), any(OverrideStatus.class));
    }

    @Test
    @DisplayName("Given the turnOnTemperature of the PumpConfig has changed" +
            "When the UpdatedPumpConfig class receives the update" +
            "Then it knows the turnOnTemperature has changed and updates the TurnOnTemperatureObservable class and notifies the AutomaticPumpToggler observers of the change")
    void canNotifyOfUpdatedTurnOnTemperature() {
        // Given
        PumpConfig initialPumpConfig = new PumpConfig(10.00, OverrideStatus.NONE);
        updatedPumpConfigSubscriber.setInitialPumpConfig(initialPumpConfig);
        PumpConfig newPumpConfig = new PumpConfig();
        newPumpConfig.setTurnOnTemp(20.00);
        pumpTurnOnTempObservable.addObserver(automaticPumpToggler);

        // When
        binding.updatedPumpConfigInput().send(MessageBuilder.withPayload(newPumpConfig).build());

        // Then
        verify(automaticPumpToggler).update(any(Observable.class), any(Double.class));
    }
}