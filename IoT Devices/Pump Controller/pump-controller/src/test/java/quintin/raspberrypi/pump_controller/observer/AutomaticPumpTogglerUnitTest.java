package quintin.raspberrypi.pump_controller.observer;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import quintin.raspberrypi.pump_controller.data.OverrideStatus;
import quintin.raspberrypi.pump_controller.data.PumpConfig;
import quintin.raspberrypi.pump_controller.domain.AmbientTempReader;
import quintin.raspberrypi.pump_controller.domain.PumpToggler;
import quintin.raspberrypi.pump_controller.observable.PumpOverrideObservable;
import quintin.raspberrypi.pump_controller.observable.TurnOnTempObservable;

import java.util.concurrent.ScheduledExecutorService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {
        AutomaticPumpToggler.class,
        PumpConfig.class,
        TurnOnTempObservable.class,
        PumpOverrideObservable.class,
        PumpToggler.class,
})
class AutomaticPumpTogglerUnitTest {

    @MockBean
    private ScheduledExecutorService scheduledExecutorService;

    @Autowired
    private PumpConfig pumpConfig;

    @Autowired
    private TurnOnTempObservable turnOnTempObservable;

    @Autowired
    private PumpOverrideObservable pumpOverrideObservable;

    @SpyBean
    private AutomaticPumpToggler automaticPumpToggler;

    @MockBean
    private PumpToggler pumpToggler;

    @MockBean
    private AmbientTempReader ambientTempReaderMock;

    @Captor
    private ArgumentCaptor<Double> turnOnTempCaptor;

    @Captor
    private ArgumentCaptor<OverrideStatus> overrideStatusCaptor;

    @DisplayName(
            "Given the AutomaticPumpToggler is an observer of both TurnOnTemperatureObservable and OverrideStatusObservable" +
                    "When TurnOnTemperature is changed" +
                    "Then the AutomaticPumpToggler is notified and receives the updated TurnOnTemperature"
    )
    @Test
    void canNotifyAutomaticPumpTogglerOfTurnOnTemperatureChange() {
        doNothing().when(automaticPumpToggler).run();
        doNothing().when(pumpToggler).turnOnPump();
        doNothing().when(pumpToggler).turnOffPump();

        // Given
        turnOnTempObservable.addObserver(automaticPumpToggler);
        pumpOverrideObservable.addObserver(automaticPumpToggler);

        // When
        turnOnTempObservable.setTurnOnTemp(20.00);
        turnOnTempObservable.notifyObservers(this.turnOnTempObservable.getTurnOnTemp());

        // Then
        verify(automaticPumpToggler).update(any(), turnOnTempCaptor.capture());
        Double receivedTurnOnTemp = turnOnTempCaptor.getValue();
        assertThat(receivedTurnOnTemp).isEqualTo(20.00);
    }

    @DisplayName(
            "Given the AutomaticPumpToggler is an observer of both TurnOnTemperatureObservable and OverrideStatusObservable" +
                    "When OverrideStatus is changed" +
                    "Then the AutomaticPumpToggler is notified and receives the updated OverrideStatus"
    )
    @Test
    void canNotifyAutomaticPumpTogglerOfManualOverride() {
        doNothing().when(automaticPumpToggler).run();
        doNothing().when(pumpToggler).turnOnPump();
        doNothing().when(pumpToggler).turnOffPump();

        // Given
        turnOnTempObservable.addObserver(automaticPumpToggler);
        pumpOverrideObservable.addObserver(automaticPumpToggler);

        // When
        pumpOverrideObservable.setOverrideStatus(OverrideStatus.PUMP_OFF);
        pumpOverrideObservable.notifyObservers(this.pumpOverrideObservable.getOverrideStatus());

        // Then
        verify(automaticPumpToggler).update(any(), overrideStatusCaptor.capture());
        OverrideStatus receivedOverrideStatus = overrideStatusCaptor.getValue();
        assertThat(receivedOverrideStatus).isEqualTo(OverrideStatus.PUMP_OFF);
    }

    @DisplayName(
            "Given the AmbientTemperatureReader's readTemperature method returns 25.00 and the TurnOnTemperature is 20.00" +
                    "When the AutomaticToggler in in run mode" +
                    "Then the AutomaticPumpToggler is turned on"
    )
    @Test
    void canTurnOnPumpWhenAmbientTempMoreThanTurnOnTemp() throws Exception {
        // Given
        when(ambientTempReaderMock.readTemp()).thenReturn(25.00);
        turnOnTempObservable.addObserver(automaticPumpToggler);
        turnOnTempObservable.setTurnOnTemp(20.00);
        turnOnTempObservable.notifyObservers(this.turnOnTempObservable.getTurnOnTemp());
        Thread.sleep(11000); // run method is only invoked after 10 seconds according to scheduledExecutorService.scheduleAtFixedRate method

        // Then
        verify(pumpToggler).turnOnPump();

    }

    @DisplayName(
            "Given the AmbientTemperatureReader's readTemperature method returns 25.00 and the TurnOnTemperature is 26.00" +
                    "When the AutomaticToggler in in run mode" +
                    "Then the AutomaticPumpToggler is turned off"
    )
    @Test
    void canTurnOffPumpWhenAmbientTempLessThanTurnOnTemp() throws Exception {
        // Given
        when(ambientTempReaderMock.readTemp()).thenReturn(25.00);
        turnOnTempObservable.addObserver(automaticPumpToggler);
        turnOnTempObservable.setTurnOnTemp(26.00);
        turnOnTempObservable.notifyObservers(this.turnOnTempObservable.getTurnOnTemp());
        Thread.sleep(11000); // run method is only invoked after 10 seconds according to scheduledExecutorService.scheduleAtFixedRate method

        // Then
        verify(pumpToggler).turnOffPump();

    }


}