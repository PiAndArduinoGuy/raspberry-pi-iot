package quintin.raspberrypi.control_hub.util;

import org.springframework.cloud.stream.messaging.Sink;
import org.springframework.http.HttpStatus;
import org.springframework.messaging.support.MessageBuilder;
import quintin.raspberrypi.control_hub.channel.ControlHubChannels;
import quintin.raspberrypi.control_hub.exception.Problem;

import static org.assertj.core.api.Assertions.assertThat;

public class TestUtil {
    public static void sendFifteenAmbientTempReadingsUsingBinding(ControlHubChannels binding) {
        for(int i=20; i <35; i++){
            binding.newAmbientTempInput().send(MessageBuilder.withPayload(i).build());
        }
    }

    public static void assertZalandoProblem(final Problem zalandoProblem, final HttpStatus httpStatus, final String detail) {
        assertThat(zalandoProblem.getTitle()).isEqualToIgnoringCase(httpStatus.getReasonPhrase());
        assertThat(zalandoProblem.getDetail()).isEqualToIgnoringCase(detail);
        assertThat(zalandoProblem.getStatus()).isEqualTo(httpStatus.value());
    }
}
