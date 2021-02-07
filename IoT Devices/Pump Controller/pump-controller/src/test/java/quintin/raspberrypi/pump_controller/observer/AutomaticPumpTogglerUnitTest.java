package quintin.raspberrypi.pump_controller.observer;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.client.RestTemplate;
import quintin.raspberrypi.pump_controller.data.OverrideStatus;
import quintin.raspberrypi.pump_controller.data.PumpConfig;
import quintin.raspberrypi.pump_controller.domain.AmbientTempReader;
import quintin.raspberrypi.pump_controller.domain.PumpToggler;
import quintin.raspberrypi.pump_controller.observable.NewAmbientTempReadingObservable;
import quintin.raspberrypi.pump_controller.observable.PumpOverrideStatusObservable;
import quintin.raspberrypi.pump_controller.observable.PumpTurnOnTempObservable;
import quintin.raspberrypi.pump_controller.runner.PumpControllerInitializer;

import java.util.concurrent.ScheduledExecutorService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SpringBootTest
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class AutomaticPumpTogglerUnitTest {

    @MockBean
    private ScheduledExecutorService scheduledExecutorService;

    @MockBean
    private RestTemplate controlHubBaseRestTemplateMock;

    @MockBean
    private PumpControllerInitializer pumpControllerInitializer;

    @Autowired
    private PumpConfig pumpConfig;

    @Autowired
    private PumpTurnOnTempObservable pumpTurnOnTempObservable;

    @Autowired
    private PumpOverrideStatusObservable pumpOverrideStatusObservable;

    @Autowired
    private NewAmbientTempReadingObservable newAmbientTempReadingObservable;

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

    @Captor
    private ArgumentCaptor<Double> newAmbientTempReadingCaptor;

    @DisplayName(
            "Given the AutomaticPumpToggler is an observer of TurnOnTemperatureObservable" +
                    "When TurnOnTemperature is changed" +
                    "Then the AutomaticPumpToggler is notified and receives the updated TurnOnTemperature"
    )
    @Test
    void canNotifyAutomaticPumpTogglerOfTurnOnTemperatureChange() {
        doNothing().when(automaticPumpToggler).run();
        doNothing().when(pumpToggler).turnOnPump();
        doNothing().when(pumpToggler).turnOffPump();

        // Given
        pumpTurnOnTempObservable.addObserver(automaticPumpToggler);

        // When
        pumpTurnOnTempObservable.setTurnOnTemp(20.00);
        pumpTurnOnTempObservable.notifyObservers(this.pumpTurnOnTempObservable.getTurnOnTemp());

        // Then
        verify(automaticPumpToggler).update(any(PumpTurnOnTempObservable.class), turnOnTempCaptor.capture());
        Double receivedTurnOnTemp = turnOnTempCaptor.getValue();
        assertThat(receivedTurnOnTemp).isEqualTo(20.00);
    }

    @DisplayName(
            "Given the AutomaticPumpToggler is an observer of OverrideStatusObservable" +
                    "When OverrideStatus is changed" +
                    "Then the AutomaticPumpToggler is notified and receives the updated OverrideStatus"
    )
    @Test
    void canNotifyAutomaticPumpTogglerOfManualOverride() {
        doNothing().when(automaticPumpToggler).run();
        doNothing().when(pumpToggler).turnOnPump();
        doNothing().when(pumpToggler).turnOffPump();

        // Given
        pumpOverrideStatusObservable.addObserver(automaticPumpToggler);

        // When
        pumpOverrideStatusObservable.setOverrideStatus(OverrideStatus.PUMP_OFF);
        pumpOverrideStatusObservable.notifyObservers(this.pumpOverrideStatusObservable.getOverrideStatus());

        // Then
        verify(automaticPumpToggler).update(any(PumpOverrideStatusObservable.class), overrideStatusCaptor.capture());
        OverrideStatus receivedOverrideStatus = overrideStatusCaptor.getValue();
        assertThat(receivedOverrideStatus).isEqualTo(OverrideStatus.PUMP_OFF);
    }


    @Test
    @DisplayName("Given AutomaticPumpToggler is an observer of NewAmbientTempReadingObservable" +
            "When NewAmbientTempReadingObservable's tempReading changes" +
            "Then the AutomaticPumpToggler receives the new reading from the NewAmbientTempReadingObservable and can add it to ambient temperature readings list")
    void automaticPumpTogglerCanSumAmbientTemperatures(){
        // Given
        newAmbientTempReadingObservable.addObserver(automaticPumpToggler);

        // When
        newAmbientTempReadingObservable.setTemp(10.00);
        newAmbientTempReadingObservable.notifyObservers(newAmbientTempReadingObservable.getTempReading());

        // Then
        verify(automaticPumpToggler).update(any(NewAmbientTempReadingObservable.class), newAmbientTempReadingCaptor.capture());
        Double newAmbientTempReading = newAmbientTempReadingCaptor.getValue();
        assertThat(newAmbientTempReading).isEqualTo(10.0);
    }

    @Test
    @DisplayName("Given AutomaticPumpToggler is an observer of NewAmbientTempReadingObservable" +
            "When NewAmbientTempReadingObservable's tempReading changes and observers are notified" +
            "Then the AutomaticPumpToggler receives the new reading from the NewAmbientTempReadingObservable")
    void canNotifyAutomaticPumpTogglerOfNewTempReading(){
        // Given
        newAmbientTempReadingObservable.addObserver(automaticPumpToggler);

        // When
        newAmbientTempReadingObservable.setTemp(10.00);
        newAmbientTempReadingObservable.notifyObservers(10.00);

        // Then
        verify(automaticPumpToggler).update(any(NewAmbientTempReadingObservable.class),any(Double.class));
    }

    @Test
    @DisplayName("Given the AmbientTempReader has given 5 temps greater than 30" +
            "When the AutomaticPumpToggler needs to perform its check with an interval of 5 seconds " +
            "Then the average of the 5 temperatures is used to determine if the PumpToggler be toggled on")
    void canTogglePumpOnBasedOnCriteria() throws Exception {
        automaticPumpToggler.update(pumpTurnOnTempObservable, 30.00); // set turn on temp for test to drive expected behaviour

        simulateFiveGreaterThan30AmbientTempReadings();

        verify(automaticPumpToggler, atMost(6)).update(any(),any()); // additional call is for turnOnTempObservable
        verify(pumpToggler, timeout(7000)).turnOnPump();
    }

    @Test
    @DisplayName("Given the AmbientTempReader has given 5 temps less than 30" +
            "When the AutomaticPumpToggler needs to perform its check with an interval of 5 seconds " +
            "Then the average of the 5 temperatures is used to determine if the PumpToggler be toggled off")
    void canTogglePumpOffBasedOnCriteria() throws Exception {
        automaticPumpToggler.update(pumpTurnOnTempObservable, 30.00); // set turn on temp for test to drive expected behaviour

        simulateFiveLessThan30AmbientTempReadings();

        verify(automaticPumpToggler, atMost(6)).update(any(),any()); // additional call is for turnOnTempObservable
        verify(pumpToggler, timeout(7000)).turnOffPump();
    }

    private void simulateFiveGreaterThan30AmbientTempReadings() {
        newAmbientTempReadingObservable.addObserver(automaticPumpToggler);
        newAmbientTempReadingObservable.setTemp(34.00);
        newAmbientTempReadingObservable.notifyObservers(newAmbientTempReadingObservable.getTempReading());
        newAmbientTempReadingObservable.setTemp(31.00);
        newAmbientTempReadingObservable.notifyObservers(newAmbientTempReadingObservable.getTempReading());
        newAmbientTempReadingObservable.setTemp(30.00);
        newAmbientTempReadingObservable.notifyObservers(newAmbientTempReadingObservable.getTempReading());
        newAmbientTempReadingObservable.setTemp(32.00);
        newAmbientTempReadingObservable.notifyObservers(newAmbientTempReadingObservable.getTempReading());
        newAmbientTempReadingObservable.setTemp(39.00);
        newAmbientTempReadingObservable.notifyObservers(newAmbientTempReadingObservable.getTempReading());
    }

    private void simulateFiveLessThan30AmbientTempReadings() {
        newAmbientTempReadingObservable.addObserver(automaticPumpToggler);
        newAmbientTempReadingObservable.setTemp(29.00);
        newAmbientTempReadingObservable.notifyObservers(newAmbientTempReadingObservable.getTempReading());
        newAmbientTempReadingObservable.setTemp(28.00);
        newAmbientTempReadingObservable.notifyObservers(newAmbientTempReadingObservable.getTempReading());
        newAmbientTempReadingObservable.setTemp(27.00);
        newAmbientTempReadingObservable.notifyObservers(newAmbientTempReadingObservable.getTempReading());
        newAmbientTempReadingObservable.setTemp(26.00);
        newAmbientTempReadingObservable.notifyObservers(newAmbientTempReadingObservable.getTempReading());
        newAmbientTempReadingObservable.setTemp(25.00);
        newAmbientTempReadingObservable.notifyObservers(newAmbientTempReadingObservable.getTempReading());
    }


}