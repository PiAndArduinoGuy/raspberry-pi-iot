package quintin.raspberrypi.pump_controller.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.DefaultUriBuilderFactory;

@Configuration
public class ControlHubRestTemplateConfig {

    @Value("${control-hub.host}")
    private String controlHubHost;

    @Value("${control-hub.port}")
    private String controlHubPort;

    @Bean
    public RestTemplate controlHubBaseRestTemplate() {
        RestTemplate restTemplate = new RestTemplate();
        restTemplate.setUriTemplateHandler(new DefaultUriBuilderFactory("http://" + controlHubHost + ":" + controlHubPort + "control-hub-backend"));
        return restTemplate;
    }
}
