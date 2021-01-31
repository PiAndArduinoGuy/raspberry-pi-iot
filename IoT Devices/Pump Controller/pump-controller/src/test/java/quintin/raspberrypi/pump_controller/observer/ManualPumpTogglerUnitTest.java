package quintin.raspberrypi.pump_controller.observer;

import org.junit.jupiter.api.BeforeEach;
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
import quintin.raspberrypi.pump_controller.domain.PumpToggler;
import quintin.raspberrypi.pump_controller.observable.PumpOverrideStatusObservable;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {
        PumpOverrideStatusObservable.class,
        ManualPumpToggler.class
})
public class ManualPumpTogglerUnitTest {
    @Autowired
    private PumpOverrideStatusObservable pumpOverrideStatusObservable;

    @SpyBean
    private ManualPumpToggler manualPumpToggler;

    @MockBean
    private PumpToggler pumpToggler;

    @Captor
    ArgumentCaptor<OverrideStatus> overrideStatusCaptor;

    @BeforeEach
    void setUp(){
        pumpOverrideStatusObservable.addObserver(manualPumpToggler);
    }

    @Test
    @DisplayName("Given the ManualPumpToggler is an Observer of PumpOverrideObservable" +
            "When OverrideStatus changes state" +
            "Then the ManualPumpToggler receives the updated OverrideStatus")
    void canReceiveUpdatedWhenOverrideStatusHasBeenChanged(){
        // When
        pumpOverrideStatusObservable.setOverrideStatus(OverrideStatus.NONE);
        pumpOverrideStatusObservable.notifyObservers(this.pumpOverrideStatusObservable.getOverrideStatus());

        // Then
        verify(manualPumpToggler).update(any(), overrideStatusCaptor.capture());
        OverrideStatus receivedOverrideStatus = overrideStatusCaptor.getValue();
        assertThat(receivedOverrideStatus).isEqualTo(OverrideStatus.NONE);
    }

    @Test
    @DisplayName("Given the ManualPumpToggler is an Observer of PumpOverrideObservable " +
            "When the OverrideStatus is set to PUMP_ON" +
            "Then the ManualPumpToggler invokes the turnOn() method of PumpToggler class")
    void canTurnPumpOnWhenOverrideStatusIsPUMP_ON(){
       // When
        pumpOverrideStatusObservable.setOverrideStatus(OverrideStatus.PUMP_ON);
        pumpOverrideStatusObservable.notifyObservers(this.pumpOverrideStatusObservable.getOverrideStatus());

        // Then
        verify(pumpToggler).turnOnPump();

    }

    @Test
    @DisplayName("Given the ManualPumpToggler is an Observer of PumpOverrideObservable " +
            "When the OverrideStatus is set to PUMP_OFF" +
            "Then the ManualToggler invokes the turnOff() method of PumpToggler class")
    void canTurnPumpOffWhenOverrideStatusIsPUMP_OFF(){
        // When
        pumpOverrideStatusObservable.setOverrideStatus(OverrideStatus.PUMP_OFF);
        pumpOverrideStatusObservable.notifyObservers(this.pumpOverrideStatusObservable.getOverrideStatus());

        // Then
        verify(pumpToggler).turnOffPump();

    }
}
