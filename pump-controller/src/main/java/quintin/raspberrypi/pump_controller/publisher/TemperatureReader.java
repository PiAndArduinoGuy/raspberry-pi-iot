package quintin.raspberrypi.pump_controller.publisher;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.concurrent.TimeoutException;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class TemperatureReader implements Runnable{
    private double temperature;
    private final static String TEMPERATURE_QUEUE = "temperature_queue";
    private final ConnectionFactory connectionFactory;

    public TemperatureReader(){
        this.connectionFactory = new ConnectionFactory();
        this.connectionFactory.setHost("localhost");
    }

    private void setTemperature(){
        log.info("Thermistor code here");
        this.temperature = 20.00;
    }

    @Override
    public void run(){
        try(Connection connection = this.connectionFactory.newConnection();
            Channel channel = connection.createChannel()){
            channel.queueDeclare(TEMPERATURE_QUEUE, false, false, false, null);
            this.setTemperature();
            byte[] temperatureMessage = ByteBuffer.allocate(Double.BYTES).putDouble(this.temperature).array();
            channel.basicPublish("", TEMPERATURE_QUEUE, null, temperatureMessage);
            log.info("Temperature obtained, sending to subscriber now");
        } catch (TimeoutException e) {
            e.printStackTrace();
            log.error(e.getMessage());
        } catch (IOException e) {
            e.printStackTrace();
            log.error(e.getMessage());
        }

    }
}
