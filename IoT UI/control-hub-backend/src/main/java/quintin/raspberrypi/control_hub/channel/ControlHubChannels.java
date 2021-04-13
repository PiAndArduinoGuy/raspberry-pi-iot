package quintin.raspberrypi.control_hub.channel;

import org.springframework.cloud.stream.annotation.Input;
import org.springframework.cloud.stream.annotation.Output;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.SubscribableChannel;

public interface ControlHubChannels {
    String NEW_AMBIENT_TEMP_INPUT = "newAmbientTempInput";
    String PUMP_STATE_INPUT = "pumpStateInput";
    String NEW_PUMP_CONFIG_OUTPUT = "newPumpConfigOutput";

    @Input(NEW_AMBIENT_TEMP_INPUT)
    SubscribableChannel newAmbientTempInput();

    @Input(PUMP_STATE_INPUT)
    SubscribableChannel pumpStateInput();

    @Output
    MessageChannel newPumpConfigOutput();

}
