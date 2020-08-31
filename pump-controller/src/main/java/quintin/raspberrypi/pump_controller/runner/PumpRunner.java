package quintin.raspberrypi.pump_controller.runner;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.ResourceUtils;
import org.springframework.web.client.RestTemplate;
import quintin.raspberrypi.pump_controller.data.PumpConfig;
import quintin.raspberrypi.pump_controller.subscriber.PumpAutonomousToggler;
import quintin.raspberrypi.pump_controller.subscriber.UpdatedPumpConfigSubscriber;
import quintin.raspberrypi.pump_controller.utils.PumpConfigUtils;

@Slf4j
@Component
public class PumpRunner implements CommandLineRunner {
    private RestTemplate restTemplate;
    private UpdatedPumpConfigSubscriber updatedPumpConfigSubscriber;
    private PumpAutonomousToggler pumpAutonomousToggler;

    public PumpRunner() throws IOException, TimeoutException {
        this.restTemplate = new RestTemplate();
        setInitialPumpConfig();
        // subscriber to listen to changed made to pump config
        this.updatedPumpConfigSubscriber = new UpdatedPumpConfigSubscriber();
        this.updatedPumpConfigSubscriber.subscribe();

        // subscriber to listen to incoming temperature values
        this.pumpAutonomousToggler = new PumpAutonomousToggler();
        this.pumpAutonomousToggler.performAutonomousPumpToggling();

    }

    @Override
    public void run(final String... args){
        performTask();
    }

    private void performTask() {

    }

    private void setInitialPumpConfig() {
        ResponseEntity<PumpConfig> responseEntity = this.restTemplate.getForEntity("http://localhost:8080/pump-configuration", PumpConfig.class);
        if (responseEntity.getStatusCode().is2xxSuccessful()){
            log.info(String.format("Initial pump config obtained - %s", responseEntity.getBody()));
            PumpConfigUtils.saveUpdatedPumpConfig(responseEntity.getBody());
        } else{
            log.error(String.format("Control hub could not be reached, response is \n %s", responseEntity));
        }
    }
}
