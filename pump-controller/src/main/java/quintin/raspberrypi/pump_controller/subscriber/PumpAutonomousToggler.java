package quintin.raspberrypi.pump_controller.subscriber;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.concurrent.TimeoutException;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.ResourceUtils;
import quintin.raspberrypi.pump_controller.data.PumpConfig;

@Slf4j
public class PumpAutonomousToggler{
    private final static String TEMPERATURE_QUEUE = "temperature_queue";
    private ConnectionFactory connectionFactory;

    public PumpAutonomousToggler(){
        this.connectionFactory = new ConnectionFactory();
        this.connectionFactory.setHost("localhost");
    }

    public void performAutonomousPumpToggling(){
        try {
            Connection connection = this.connectionFactory.newConnection();
            Channel channel = connection.createChannel();
            channel.queueDeclare(TEMPERATURE_QUEUE, false,false,false,null);

            DeliverCallback deliverCallback = (consumerTag, delivery) -> {
                double temperature = ByteBuffer.wrap(delivery.getBody()).getDouble();
                log.info(String.format("Temperature received, value is %s", temperature));
                this.togglePump(temperature);
            };
            channel.basicConsume(TEMPERATURE_QUEUE, true, deliverCallback, consumerTag -> {});

        } catch (IOException e) {
            e.printStackTrace();
        } catch (TimeoutException e) {
            e.printStackTrace();
        }

    }

    private void togglePump(double temperature){
        log.info("Performing autonomous toggling task");
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            PumpConfig pumpConfig = objectMapper.readValue(ResourceUtils.getFile("classpath:pump/pump_config.json"), PumpConfig.class);
            if (temperature < pumpConfig.getTurnOffTemp()){
                log.info("Turn off pump code here");
            } else if (temperature > pumpConfig.getTurnOffTemp()){
                log.info("Turn on pump code here");
            }
        } catch (IOException e) {
            log.error(e.getMessage());
            e.printStackTrace();
        }
    }

}
