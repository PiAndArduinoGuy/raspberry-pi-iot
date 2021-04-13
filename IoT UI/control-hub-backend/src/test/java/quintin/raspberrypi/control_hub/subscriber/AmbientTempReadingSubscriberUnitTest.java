package quintin.raspberrypi.control_hub.subscriber;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.stream.messaging.Sink;
import org.springframework.cloud.stream.test.binder.MessageCollector;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.test.annotation.DirtiesContext;
import quintin.raspberrypi.control_hub.observable.LatestFifteenAmbientTempReadingsObservable;
import quintin.raspberrypi.control_hub.util.TestUtil;

import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@SpringBootTest
class AmbientTempReadingSubscriberUnitTest {
    @Autowired
    private MessageCollector messageCollector;

    @Autowired
    private Sink binding;

    @Autowired
    private LatestFifteenAmbientTempReadingsObservable latestFifteenAmbientTempReadingsObservable;

    @Autowired
    private AmbientTempReadingSubscriber ambientTempReadingSubscriber;

    // cannot test with these as the get method used here has been removed

//    @Test
//    @DisplayName("Given that the ambient temperature 10.00 is published" +
//            " When the AmbientTempReadingSubscriber receives the message" +
//            " Then the ambient temperature reading is added to the AmbientTempReadings list")
//    @DirtiesContext
//    void canReceiveAnAmbientTempReading(){
//        binding.input().send(MessageBuilder.withPayload(10.00).build());
//
//        List<Double> ambientTempReadings = ambientTempReadingSubscriber.getAmbientTempReadings();
//
//        assertThat(ambientTempReadings.size()).isEqualTo(1);
//        assertThat(ambientTempReadings.get(0)).isEqualTo(10.00);
//    }
//
//    @Test
//    @DisplayName("Given that the ambient temperatures 10.00 and 11.00 are published" +
//            " When the AmbientTempReadingSubscriber receives the message" +
//            " Then the ambient temperature readings are added to the AmbientTempReadings list")
//    @DirtiesContext
//    void canReceiveMultipleAmbientTempReadings(){
//        binding.input().send(MessageBuilder.withPayload(10.00).build());
//        binding.input().send(MessageBuilder.withPayload(11.00).build());
//
//        List<Double> ambientTempReadings = ambientTempReadingSubscriber.getAmbientTempReadings();
//
//        assertThat(ambientTempReadings.size()).isEqualTo(2);
//        assertThat(ambientTempReadings.get(0)).isEqualTo(10.00);
//        assertThat(ambientTempReadings.get(1)).isEqualTo(11.00);
//    }
//
//    @Test
//    @DisplayName("Given that the ambient temperatures sent in a 15 min cycle is 15" +
//            " When the 16th reading is received" +
//            " Then the previous 15 are cleared and the AmbientTempReadings list only contains the 16th element")
//    @DirtiesContext
//    void canResetAmbientTempReadingsListAfter15ReadingsReceived(){
//        TestUtil.sendFifteenAmbientTempReadingsUsingBinding(binding);
//
//        List<Double> ambientTempReadingsWith15Readings = ambientTempReadingSubscriber.getAmbientTempReadings();
//        assertThat(ambientTempReadingsWith15Readings.size()).isEqualTo(15);
//
//        binding.input().send(MessageBuilder.withPayload(36.00).build());
//
//        List<Double> ambientTempReadingsWith1Reading = ambientTempReadingSubscriber.getAmbientTempReadings();
//        assertThat(ambientTempReadingsWith1Reading.size()).isEqualTo(1);
//    }

}
