package quintin.raspberrypi.pump_controller;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import quintin.raspberrypi.pump_controller.data.PumpConfig;

@SpringBootApplication
public class PumpControllerApplication {

    public static void main(String... args) {
        SpringApplication.run(PumpControllerApplication.class, args);
    }
}
