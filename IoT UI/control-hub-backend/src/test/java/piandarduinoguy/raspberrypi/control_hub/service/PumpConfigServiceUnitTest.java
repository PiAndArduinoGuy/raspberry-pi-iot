package piandarduinoguy.raspberrypi.control_hub.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import piandarduinoguy.raspberrypi.control_hub.OverrideStatus;
import piandarduinoguy.raspberrypi.control_hub.PumpConfig;
import piandarduinoguy.raspberrypi.control_hub.exception.RaspberryPiControlHubException;
import piandarduinoguy.raspberrypi.control_hub.observable.LatestAmbientTempReadingObservable;
import piandarduinoguy.raspberrypi.control_hub.observable.LatestFifteenAmbientTempReadingsObservable;
import piandarduinoguy.raspberrypi.control_hub.observer.LatestTemperaturesObserver;


import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Fail.fail;

@SpringBootTest
@TestPropertySource("classpath:application-test.properties")
class PumpConfigServiceUnitTest {
    @Autowired
    private PumpControllerService pumpControllerService;

    @Autowired
    private LatestAmbientTempReadingObservable latestAmbientTempReadingObservable;

    @Autowired
    private LatestFifteenAmbientTempReadingsObservable latestFifteenAmbientTempReadingsObservable;

    @Autowired
    private PumpConfigService pumpConfigService;

    @Autowired
    private LatestTemperaturesObserver latestTemperaturesObserver;

    @Autowired
    private ObjectMapper objectMapper;

    @Value("${pump-config-file-location}")
    private String pumpConfigFileLocation;

    @Test
    void canGetPumpConfig() {
        try {
            objectMapper.writeValue(new File(pumpConfigFileLocation), new PumpConfig(20.00, OverrideStatus.NONE));
        } catch (IOException e) {
            fail("An IOException was thrown while preparing the test: ", e);
        }

        PumpConfig pumpConfig = pumpConfigService.getPumpConfig();

        assertThat(pumpConfig).isNotNull();
        assertThat(pumpConfig.getTurnOffTemp()).isEqualTo(20.00);
        assertThat(pumpConfig.getOverrideStatus()).isEqualTo(OverrideStatus.NONE);
    }

    @Test
    void canSaveNewPumpConfig() {
        pumpConfigService.saveNewConfig(new PumpConfig(22.00, OverrideStatus.PUMP_ON));

        PumpConfig pumpConfig = pumpConfigService.getPumpConfig();
        assertThat(pumpConfig.getTurnOffTemp()).isEqualTo(22.00);
        assertThat(pumpConfig.getOverrideStatus()).isEqualTo(OverrideStatus.PUMP_ON);
    }

    @Test
    @DisplayName("Given PumpControllerService is an observer of LatestAmbientTempReadingObservable" +
            " When the latestAmbientTempReading attribute changes state in LatestAmbientTempReadingObservable" +
            " Then the latestAmbientTemp attribute in the PumpControllerService is updated")
    void latestAmbientTempReadingObserverActsWhenObservableChangesState(){
        latestAmbientTempReadingObservable.addObserver(latestTemperaturesObserver);

        latestAmbientTempReadingObservable.setLatestAmbientTempReading(12.00);

        double latestAmbientTempReading = pumpControllerService.getLatestAmbientTempReading();
        assertThat(latestAmbientTempReading).isEqualTo(12.00);
    }

    @Test
    @DisplayName("Given PumpControllerService is an observer of LatestFifteenAmbientTempReadingsObservable" +
            " When the latestFifteenAmbientTempReadings attribute changes state in LatestFifteenAmbientTempReadingsObservable" +
            " Then the latestFifteenAmbientTempReadingAvg is calculated and stored in the PumpControllerService")
    void withLatestFifteenAmbientTempReadingsCanCalculateLatestFifteenAmbientTempReadingAvg(){
        latestFifteenAmbientTempReadingsObservable.addObserver(latestTemperaturesObserver);

        List<Double> latestFifteenAmbientTempReadings = new ArrayList<>();
        for(int i=20; i <35; i++){
            latestFifteenAmbientTempReadings.add(Double.parseDouble(Integer.toString(i)));
        }
        latestFifteenAmbientTempReadingsObservable.setLatestFifteenAmbientTempReadings(latestFifteenAmbientTempReadings);

        Double latestFifteenAmbientTempReadingsAvg = pumpControllerService.getLatestFifteenAmbientTempReadingsAvg();
        assertThat(latestFifteenAmbientTempReadingsAvg).isEqualTo(27.00);
    }

    @Test
    @DisplayName("Given the latestAmbientTempReading has not been received" +
            " When the PumpControllerService's getLatestAmbientTempReading " +
            " Then throw RaspberryPiControlHubException with message 'An ambient temperature has not yet been sent.'")
    void canThrowExceptionWhenNoAmbientTempReading(){
        assertThatThrownBy(() ->{
            pumpControllerService.getLatestAmbientTempReading();
        }).isInstanceOf(RaspberryPiControlHubException.class)
                .hasMessage("An ambient temperature has not yet been sent.");
    }

    @Test
    @DisplayName("Given the latestFifteenAmbientTempReading has not been received" +
            " When the PumpControllerService's getLatestAmbientTempReading " +
            " Then throw RaspberryPiControlHubException with message 'An ambient temperature has not yet been sent.'")
    void canThrowExceptionWhenNoLatestFifteenAmbientTempReadings(){
        assertThatThrownBy(() ->{
            pumpControllerService.getLatestFifteenAmbientTempReadingsAvg();
        }).isInstanceOf(RaspberryPiControlHubException.class)
                .hasMessage("15 ambient temperature readings have not yet been captured, an average could not be calculated");
    }
}
