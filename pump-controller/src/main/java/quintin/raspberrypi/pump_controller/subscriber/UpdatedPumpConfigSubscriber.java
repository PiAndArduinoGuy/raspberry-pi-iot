package quintin.raspberrypi.pump_controller.subscriber;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.ResourceUtils;
import org.springframework.web.client.RestTemplate;
import quintin.raspberrypi.pump_controller.runner.PumpRunner;
import quintin.raspberrypi.pump_controller.data.PumpConfig;
import quintin.raspberrypi.pump_controller.utils.PumpConfigUtils;

@Slf4j
public class UpdatedPumpConfigSubscriber {
    private final String QUEUE_NAME = "controller";
    private ConnectionFactory connectionFactory;
    private RestTemplate restTemplate;

    public UpdatedPumpConfigSubscriber(){
        this.restTemplate = new RestTemplate();
        this.connectionFactory = new ConnectionFactory();
        connectionFactory.setHost("localhost");
    }

    public void subscribe() throws IOException, TimeoutException {
        Connection connection = this.connectionFactory.newConnection();
        Channel channel = connection.createChannel();
        channel.queueDeclare(QUEUE_NAME, false, false, false, null);

        DeliverCallback deliverCallback = (consumerTag, delivery) -> {
            String message = new String(delivery.getBody(), "UTF-8");
            log.info(String.format("Received - %s", message));
            ResponseEntity<PumpConfig> responseEntity = this.restTemplate.getForEntity("http://localhost:8080/pump-configuration", PumpConfig.class);
            log.info(String.format("Updated configuration is - %s", responseEntity.getBody().toString()));
            PumpConfigUtils.saveUpdatedPumpConfig(responseEntity.getBody());
        };
        channel.basicConsume(QUEUE_NAME, true, deliverCallback, consumerTag -> {});
    }

}
