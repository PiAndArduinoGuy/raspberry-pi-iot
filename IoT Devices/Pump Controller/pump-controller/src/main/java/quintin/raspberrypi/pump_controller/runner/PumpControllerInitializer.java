package quintin.raspberrypi.pump_controller.runner;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import quintin.raspberrypi.pump_controller.data.PumpConfig;
import quintin.raspberrypi.pump_controller.exception.PumpControllerException;
import quintin.raspberrypi.pump_controller.observable.PumpOverrideObservable;
import quintin.raspberrypi.pump_controller.observable.TurnOnTempObservable;
import quintin.raspberrypi.pump_controller.observer.AutomaticPumpToggler;
import quintin.raspberrypi.pump_controller.observer.ManualPumpToggler;

@Slf4j
@Component
public class PumpControllerInitializer implements CommandLineRunner {
    private RestTemplate controlHubBaseRestTemplate;
    private final PumpOverrideObservable pumpOverrideObservable;
    private final TurnOnTempObservable turnOnTempObservable;


    @Autowired
    public PumpControllerInitializer(AutomaticPumpToggler automaticPumpToggler,
                                     ManualPumpToggler manualPumpToggler,
                                     PumpOverrideObservable pumpOverrideObservable,
                                     TurnOnTempObservable turnOnTempObservable,
                                     RestTemplate controlHubBaseRestTemplate) {
        this.controlHubBaseRestTemplate = controlHubBaseRestTemplate;
        this.pumpOverrideObservable = pumpOverrideObservable;
        this.turnOnTempObservable = turnOnTempObservable;
        this.turnOnTempObservable.addObserver(automaticPumpToggler);
        this.pumpOverrideObservable.addObserver(manualPumpToggler);

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
            this.pumpOverrideObservable.setOverrideStatus(initialPumpConfig.getOverrideStatus());
            this.pumpOverrideObservable.notifyObservers(this.pumpOverrideObservable.getOverrideStatus());
            this.turnOnTempObservable.setTurnOnTemp(initialPumpConfig.getTurnOnTemp());
            this.turnOnTempObservable.notifyObservers(this.turnOnTempObservable.getTurnOnTemp());
        } else {
            PumpControllerException pumpControllerException = new PumpControllerException(String.format("The updated pump config could not be retrieved from the control hub, the response was: %s", responseEntity.getBody().toString()));
            log.error("A response other than 2xx was received from the control hub.", pumpControllerException);
            throw pumpControllerException;
        }
    }
}
