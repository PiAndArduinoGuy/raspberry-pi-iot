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
import quintin.raspberrypi.pump_controller.observable.PumpOverrideStatusObservable;
import quintin.raspberrypi.pump_controller.observable.PumpTurnOnTempObservable;

@Slf4j
@EnableBinding(Sink.class)
public class UpdatedPumpConfigSubscriber {
    private RestTemplate restTemplate;
    private PumpConfig pumpConfig;
    private PumpOverrideStatusObservable pumpOverrideStatusObservable;
    private PumpTurnOnTempObservable pumpTurnOnTempObservable;

    @Autowired
    public UpdatedPumpConfigSubscriber(PumpConfig pumpConfig, RestTemplate controlHubBaseRestTemplate, PumpTurnOnTempObservable pumpTurnOnTempObservable, PumpOverrideStatusObservable pumpOverrideStatusObservable) {
        this.restTemplate = controlHubBaseRestTemplate;
        this.pumpConfig = pumpConfig;
        this.pumpTurnOnTempObservable = pumpTurnOnTempObservable;
        this.pumpOverrideStatusObservable = pumpOverrideStatusObservable;
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
            this.pumpOverrideStatusObservable.setOverrideStatus(updatedPumpConfig.getOverrideStatus());
        } else {
            log.info("The turn on temperature has been updated.");
            this.pumpTurnOnTempObservable.setTurnOnTemp(updatedPumpConfig.getTurnOnTemp());
        }
    }

    private boolean hasOverrideStatusChanged(PumpConfig updatedPumpConfig) {
        return !this.pumpConfig.getOverrideStatus().equals(updatedPumpConfig.getOverrideStatus());
    }
}
