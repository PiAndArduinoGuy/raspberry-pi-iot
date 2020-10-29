package quintin.raspberrypi.pump_controller.subscriber;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.cloud.stream.messaging.Sink;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import quintin.raspberrypi.pump_controller.data.PumpConfig;

@Slf4j
@EnableBinding(Sink.class)
public class UpdatedPumpConfig {
    private RestTemplate restTemplate;
    private PumpConfig pumpConfig;

    @Autowired
    public UpdatedPumpConfig(PumpConfig pumpConfig){
        this.restTemplate  = new RestTemplate();
        this.pumpConfig = pumpConfig;
    }

    @StreamListener(Sink.INPUT)
    public void sendPumpUpdateConfigToObservers(String msg){
        if(msg.equals("Pump configuration updated")){
            ResponseEntity<PumpConfig> responseEntity = this.restTemplate.getForEntity("http://192.168.0.130:8080/control-hub-backend/pump-configuration", PumpConfig.class);
            this.pumpConfig.setTurnOffTemp(responseEntity.getBody().getTurnOffTemp());
            this.pumpConfig.setOverrideStatus(responseEntity.getBody().getOverrideStatus());
            this.pumpConfig.notifyObservers(this.pumpConfig);
            log.info(String.format("Received - %s", msg));
        }
    }

}
