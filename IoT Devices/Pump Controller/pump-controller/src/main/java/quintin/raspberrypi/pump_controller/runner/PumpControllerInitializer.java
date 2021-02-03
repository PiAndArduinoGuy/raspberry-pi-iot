package quintin.raspberrypi.pump_controller.runner;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import quintin.raspberrypi.pump_controller.data.PumpConfig;
import quintin.raspberrypi.pump_controller.exception.PumpControllerException;
import quintin.raspberrypi.pump_controller.observable.PumpOverrideStatusObservable;
import quintin.raspberrypi.pump_controller.observable.PumpTurnOnTempObservable;
import quintin.raspberrypi.pump_controller.observer.AutomaticPumpToggler;
import quintin.raspberrypi.pump_controller.observer.ManualPumpToggler;

@Slf4j
@Component
public class PumpControllerInitializer implements CommandLineRunner {
    private RestTemplate controlHubBaseRestTemplate;
    private final PumpOverrideStatusObservable pumpOverrideStatusObservable;
    private final PumpTurnOnTempObservable pumpTurnOnTempObservable;


    @Autowired
    public PumpControllerInitializer(AutomaticPumpToggler automaticPumpToggler,
                                     ManualPumpToggler manualPumpToggler,
                                     PumpOverrideStatusObservable pumpOverrideStatusObservable,
                                     PumpTurnOnTempObservable pumpTurnOnTempObservable,
                                     RestTemplate controlHubBaseRestTemplate) {
        this.controlHubBaseRestTemplate = controlHubBaseRestTemplate;
        this.pumpOverrideStatusObservable = pumpOverrideStatusObservable;
        this.pumpTurnOnTempObservable = pumpTurnOnTempObservable;
        this.pumpTurnOnTempObservable.addObserver(automaticPumpToggler);
        this.pumpOverrideStatusObservable.addObserver(manualPumpToggler);

    }

    @Override
    public void run(final String... args) {
        this.setInitialPumpConfig();
    }


    private void setInitialPumpConfig() {
        ResponseEntity<Object> responseEntity = this.controlHubBaseRestTemplate.getForEntity("/pump-configuration", Object.class);
        if (responseEntity.getStatusCode().is2xxSuccessful()) {
            PumpConfig initialPumpConfig = (PumpConfig) responseEntity.getBody();
            log.info(String.format("Initial pump config obtained - %s", initialPumpConfig));
            this.pumpOverrideStatusObservable.setOverrideStatus(initialPumpConfig.getOverrideStatus());
            this.pumpOverrideStatusObservable.notifyObservers(this.pumpOverrideStatusObservable.getOverrideStatus());
            this.pumpTurnOnTempObservable.setTurnOnTemp(initialPumpConfig.getTurnOnTemp());
            this.pumpTurnOnTempObservable.notifyObservers(this.pumpTurnOnTempObservable.getTurnOnTemp());
        } else {
            PumpControllerException pumpControllerException = new PumpControllerException(String.format("The updated pump config could not be retrieved from the control hub, the response was: %s", responseEntity.getBody().toString()));
            log.error("A response other than 2xx was received from the control hub.", pumpControllerException);
            throw pumpControllerException;
        }
    }
}
