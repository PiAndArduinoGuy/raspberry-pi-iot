package piandarduinoguy.raspberrypi.pump_controller.runner;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import piandarduinoguy.raspberrypi.pump_controller.data.Problem;
import piandarduinoguy.raspberrypi.pump_controller.data.PumpConfig;
import piandarduinoguy.raspberrypi.pump_controller.observer.AmbientTempReader;
import piandarduinoguy.raspberrypi.pump_controller.exception.PumpControllerException;
import piandarduinoguy.raspberrypi.pump_controller.observable.PumpOverrideStatusObservable;
import piandarduinoguy.raspberrypi.pump_controller.observable.PumpTurnOnTempObservable;
import piandarduinoguy.raspberrypi.pump_controller.observer.AutomaticPumpToggler;
import piandarduinoguy.raspberrypi.pump_controller.observer.ManualPumpToggler;
import piandarduinoguy.raspberrypi.pump_controller.subscriber.UpdatedPumpConfigSubscriber;

@Slf4j
@Component
public class PumpControllerInitializer implements CommandLineRunner {
    private final RestTemplate controlHubBaseRestTemplate;
    private final PumpOverrideStatusObservable pumpOverrideStatusObservable;
    private final PumpTurnOnTempObservable pumpTurnOnTempObservable;
    private final UpdatedPumpConfigSubscriber updatedPumpConfigSubscriber;


    @Autowired
    public PumpControllerInitializer(AutomaticPumpToggler automaticPumpToggler,
                                     ManualPumpToggler manualPumpToggler,
                                     PumpOverrideStatusObservable pumpOverrideStatusObservable,
                                     PumpTurnOnTempObservable pumpTurnOnTempObservable,
                                     RestTemplate controlHubBaseRestTemplate,
                                     AmbientTempReader ambientTempReader,
                                     UpdatedPumpConfigSubscriber updatedPumpConfigSubscriber) {
        this.controlHubBaseRestTemplate = controlHubBaseRestTemplate;
        this.pumpOverrideStatusObservable = pumpOverrideStatusObservable;
        this.pumpTurnOnTempObservable = pumpTurnOnTempObservable;
        this.pumpTurnOnTempObservable.addObserver(automaticPumpToggler);
        this.pumpOverrideStatusObservable.addObserver(manualPumpToggler);
        this.pumpOverrideStatusObservable.addObserver(ambientTempReader);
        this.updatedPumpConfigSubscriber = updatedPumpConfigSubscriber;
    }

    @Override
    public void run(final String... args) {
        this.setInitialPumpConfig();
    }


    private void setInitialPumpConfig() {
        ResponseEntity<Object> responseEntity = this.controlHubBaseRestTemplate.getForEntity("/pump-controller/pump-configuration", Object.class);
        if (responseEntity.getStatusCode().is2xxSuccessful()) { // the Object is a PumpConfig
            PumpConfig initialPumpConfig = (PumpConfig) responseEntity.getBody();
            log.info(String.format("Initial pump config obtained - %s", initialPumpConfig));
            this.updatedPumpConfigSubscriber.setInitialPumpConfig(initialPumpConfig);
            this.pumpOverrideStatusObservable.setOverrideStatus(initialPumpConfig.getOverrideStatus());
            this.pumpOverrideStatusObservable.notifyObservers(this.pumpOverrideStatusObservable.getOverrideStatus());
            this.pumpTurnOnTempObservable.setTurnOnTemp(initialPumpConfig.getTurnOnTemp());
            this.pumpTurnOnTempObservable.notifyObservers(this.pumpTurnOnTempObservable.getTurnOnTemp());
        } else { // the Object is a Problem
            Problem problem = (Problem) responseEntity.getBody();
            PumpControllerException pumpControllerException = new PumpControllerException(String.format("The updated pump config could not be retrieved from the control hub, the response was a Problem - %s.", problem));
            log.error("A response other than 2xx was received from the control hub.", pumpControllerException);
            throw pumpControllerException;
        }
    }
}
