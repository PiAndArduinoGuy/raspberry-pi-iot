package quintin.raspberrypi.pump_controller.subscriber;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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

    @Value("${control-hub.host}")
    private String controlHubHost;

    @Value("${control-hub.port}")
    private String controlHubPort;

    @Autowired
    public UpdatedPumpConfig(PumpConfig pumpConfig){
        this.restTemplate  = new RestTemplate();
        this.pumpConfig = pumpConfig;
    }

    @StreamListener(Sink.INPUT)
    public void sendPumpUpdateConfigToObservers(String msg){
        if(msg.equals("Pump configuration updated")){
            ResponseEntity<PumpConfig> responseEntity = this.restTemplate.getForEntity("http://" + controlHubHost + ":" + controlHubPort +"control-hub-backend/pump-configuration", PumpConfig.class);
            this.pumpConfig.setTurnOffTemp(responseEntity.getBody().getTurnOffTemp());
            this.pumpConfig.setOverrideStatus(responseEntity.getBody().getOverrideStatus());
            this.pumpConfig.notifyObservers(this.pumpConfig);
            log.info(String.format("Received - %s", msg));
        }
    }

}
