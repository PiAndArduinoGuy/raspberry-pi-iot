package quintin.raspberrypi.pump_controller.subscriber;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.cloud.stream.messaging.Sink;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import quintin.raspberrypi.pump_controller.data.PumpConfig;
import quintin.raspberrypi.pump_controller.exception.PumpControllerException;
import quintin.raspberrypi.pump_controller.observable.PumpOverrideObservable;
import quintin.raspberrypi.pump_controller.observable.TurnOnTempObservable;

@Slf4j
@EnableBinding(Sink.class)
public class UpdatedPumpConfig {
    private RestTemplate restTemplate;
    private PumpConfig pumpConfig;
    private PumpOverrideObservable pumpOverrideObservable;
    private TurnOnTempObservable turnOnTempObservable;

    @Autowired
    public UpdatedPumpConfig(PumpConfig pumpConfig, RestTemplate controlHubBaseRestTemplate, TurnOnTempObservable turnOnTempObservable, PumpOverrideObservable pumpOverrideObservable) {
        this.restTemplate = controlHubBaseRestTemplate;
        this.pumpConfig = pumpConfig;
        this.turnOnTempObservable = turnOnTempObservable;
        this.pumpOverrideObservable = pumpOverrideObservable;
    }

    @StreamListener(Sink.INPUT)
    public void sendPumpUpdateConfigToObservers(String msg) {
        log.info(String.format("Received - %s", msg));
        if (msg.equals("Pump configuration updated")) {
            PumpConfig updatedPumpConfig = getUpdatedPumpConfig();
            notifyRelevantObserverOfPumpConfigAttributeChange(updatedPumpConfig);
        } else {
            PumpControllerException pumpControllerException = new PumpControllerException(String.format(
                    "A message was received that is not recognized - Expected the message 'Pump configuration updated' but received '%s'"
                    , msg));
            log.error(
                    "A message was received that is not recognized", pumpControllerException);
            throw pumpControllerException;
        }
    }

    private PumpConfig getUpdatedPumpConfig() {
        ResponseEntity<Object> responseEntity = this.restTemplate.getForEntity("/pump-configuration", Object.class);
        if (responseEntity.getStatusCode().is2xxSuccessful()) {
            return (PumpConfig) responseEntity.getBody();
        }
        PumpControllerException pumpControllerException = new PumpControllerException(String.format("The updated pump config could not be retrieved from the control hub, the response was: %s", responseEntity.getBody().toString()));

        log.error("A response other than 2xx was received from the control hub.", pumpControllerException);
        throw pumpControllerException;
    }

    private void notifyRelevantObserverOfPumpConfigAttributeChange(PumpConfig updatedPumpConfig) {
        if (hasOverrideStatusChanged(updatedPumpConfig)) {
            log.info("The override status has been updated.");
            this.pumpOverrideObservable.setOverrideStatus(updatedPumpConfig.getOverrideStatus());
            this.pumpOverrideObservable.notifyObservers(this.pumpOverrideObservable.getOverrideStatus());
        } else {
            log.info("The turn on temperature has been updated.");
            this.turnOnTempObservable.setTurnOnTemp(updatedPumpConfig.getTurnOnTemp());
            this.turnOnTempObservable.notifyObservers(this.turnOnTempObservable.getTurnOnTemp());
        }
    }

    private boolean hasOverrideStatusChanged(PumpConfig updatedPumpConfig) {
        return !this.pumpConfig.getOverrideStatus().equals(updatedPumpConfig.getOverrideStatus());
    }
}
