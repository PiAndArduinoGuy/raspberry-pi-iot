package piandarduinoguy.raspberrypi.pump_controller.subscriber;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.web.client.RestTemplate;
import piandarduinoguy.raspberrypi.pump_controller.channel.PumpControllerChannels;
import piandarduinoguy.raspberrypi.pump_controller.data.PumpConfig;
import piandarduinoguy.raspberrypi.pump_controller.observable.PumpOverrideStatusObservable;
import piandarduinoguy.raspberrypi.pump_controller.observable.PumpTurnOnTempObservable;

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

    public void setInitialPumpConfig(PumpConfig pumpConfig){
        this.pumpConfig = pumpConfig;
    }

    @StreamListener(PumpControllerChannels.UPDATED_PUMP_CONFIG_INPUT)
    public void sendPumpUpdateConfigToObservers(PumpConfig newPumpConfig) {
        log.info(String.format("Received - %s", newPumpConfig.toString()));
        notifyRelevantObserverOfPumpConfigAttributeChange(newPumpConfig);
    }

    private void notifyRelevantObserverOfPumpConfigAttributeChange(PumpConfig updatedPumpConfig) {
        if (hasOverrideStatusChanged(updatedPumpConfig)) {
            log.info("The override status has been updated.");
            this.pumpConfig.setOverrideStatus(updatedPumpConfig.getOverrideStatus());
            this.pumpOverrideStatusObservable.setOverrideStatus(updatedPumpConfig.getOverrideStatus());
        }
        if (hasTurnOnTempChanged(updatedPumpConfig)){
            log.info("The turn on temperature has been updated.");
            this.pumpConfig.setTurnOnTemp(updatedPumpConfig.getTurnOnTemp());
            this.pumpTurnOnTempObservable.setTurnOnTemp(updatedPumpConfig.getTurnOnTemp());
        }
    }

    private boolean hasTurnOnTempChanged(PumpConfig updatedPumpConfig) {
        return !this.pumpConfig.getTurnOnTemp().equals(updatedPumpConfig.getTurnOnTemp());
    }

    private boolean hasOverrideStatusChanged(PumpConfig updatedPumpConfig) {
        return !this.pumpConfig.getOverrideStatus().equals(updatedPumpConfig.getOverrideStatus());
    }
}
