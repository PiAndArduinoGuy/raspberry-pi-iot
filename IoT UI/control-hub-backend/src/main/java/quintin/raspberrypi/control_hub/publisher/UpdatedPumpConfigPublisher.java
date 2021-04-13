package quintin.raspberrypi.control_hub.publisher;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.messaging.Source;

@Slf4j
@EnableBinding(Source.class) // Interfaces defining channels - OUTPUT channel is the only one for this interface
public class UpdatedPumpConfigPublisher {

}
