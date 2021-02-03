package quintin.raspberrypi.control_hub;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;

@SpringBootApplication
public class RaspberryPiControlHubApplication extends SpringBootServletInitializer {
    public static void main(String... args){
        SpringApplication.run(RaspberryPiControlHubApplication.class, args);
    }
}
