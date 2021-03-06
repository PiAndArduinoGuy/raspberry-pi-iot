package quintin.raspberrypi.pump_controller.runner;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.PropertySource;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import quintin.raspberrypi.pump_controller.data.PumpConfig;
import quintin.raspberrypi.pump_controller.observer.ManualPumpToggler;
import quintin.raspberrypi.pump_controller.observer.AutomaticPumpToggler;

@Slf4j
@Component
public class PumpControllerInitializer implements CommandLineRunner {
    private RestTemplate restTemplate;

    private PumpConfig pumpConfig;
    private AutomaticPumpToggler automaticPumpToggler;
    private ManualPumpToggler manualPumpToggler;

    @Value("${control-hub.host}")
    private String controlHubHost;

    @Value("${control-hub.port}")
    private String controlHubPort;

    @Autowired
    public PumpControllerInitializer(PumpConfig pumpConfig,
                                     AutomaticPumpToggler automaticPumpToggler,
                                     ManualPumpToggler manualPumpToggler){
        this.restTemplate = new RestTemplate();
        this.pumpConfig = pumpConfig;
        this.automaticPumpToggler = automaticPumpToggler;
        this.manualPumpToggler = manualPumpToggler;
        this.pumpConfig.addObserver(automaticPumpToggler);
        this.pumpConfig.addObserver(manualPumpToggler);

    }

    @Override
    public void run(final String... args){
        this.setInitialPumpConfig();
    }


    private void setInitialPumpConfig() {
        ResponseEntity<PumpConfig> responseEntity = this.restTemplate.getForEntity("http://" + controlHubHost + ":" + controlHubPort +"/control-hub-backend/pump-configuration", PumpConfig.class);
        if (responseEntity.getStatusCode().is2xxSuccessful()){
            log.info(String.format("Initial pump config obtained - %s", responseEntity.getBody()));
            this.pumpConfig.setOverrideStatus(responseEntity.getBody().getOverrideStatus());
            this.pumpConfig.setTurnOffTemp(responseEntity.getBody().getTurnOffTemp());
            this.pumpConfig.notifyObservers(this.pumpConfig);
        } else{
            log.error(String.format("Control hub could not be reached, response is \n %s", responseEntity));
        }
    }
}
