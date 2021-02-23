package quintin.raspberrypi.pump_controller.subscriber;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.cloud.stream.messaging.Sink;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import quintin.raspberrypi.pump_controller.channel.PumpControllerChannels;
import quintin.raspberrypi.pump_controller.data.PumpConfig;
import quintin.raspberrypi.pump_controller.exception.PumpControllerException;
import quintin.raspberrypi.pump_controller.observable.PumpOverrideStatusObservable;
import quintin.raspberrypi.pump_controller.observable.PumpTurnOnTempObservable;

@Slf4j
@EnableBinding(PumpControllerChannels.class)
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

    @StreamListener(PumpControllerChannels.UPDATED_PUMP_CONFIG_INPUT)
    public void sendPumpUpdateConfigToObservers(PumpConfig newPumpConfig) {
        log.info(String.format("Received - %s", newPumpConfig.toString()));
        notifyRelevantObserverOfPumpConfigAttributeChange(newPumpConfig);
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
