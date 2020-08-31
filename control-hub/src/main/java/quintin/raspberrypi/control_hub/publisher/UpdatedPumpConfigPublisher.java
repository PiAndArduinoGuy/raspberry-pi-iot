package quintin.raspberrypi.control_hub.publisher;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class UpdatedPumpConfigPublisher {
    private final String QUEUE_NAME = "controller";
    private ConnectionFactory connectionFactory;

    public UpdatedPumpConfigPublisher(){
        connectionFactory = new ConnectionFactory();
        connectionFactory.setHost("localhost");
    }

    public void publish() throws IOException, TimeoutException {
        try(Connection connection = this.connectionFactory.newConnection();
            Channel channel = connection.createChannel()){
            channel.queueDeclare(QUEUE_NAME, false, false, false, null);
            String message = "Pump configuration file updated";
            channel.basicPublish("", QUEUE_NAME, null, message.getBytes());
            log.info("Update sent.");
        }

    }

}
