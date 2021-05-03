package piandarduinoguy.raspberrypi.pump_controller.observer;

import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import piandarduinoguy.raspberrypi.pump_controller.observable.NewAmbientTempReadingObservable;
import piandarduinoguy.raspberrypi.pump_controller.publisher.AmbientTempPublisher;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {
        AmbientTempReaderUnitTest.class,
        NewAmbientTempReadingObservable.class,
        AmbientTempPublisher.class
})
public class AmbientTempReaderUnitTest {
    @MockBean
    private NewAmbientTempReadingObservable newAmbientTempReadingObservable;

    @MockBean
    private AmbientTempPublisher ambientTempPublisher;

    @Autowired
    private AmbientTempReaderUnitTest ambientTempReader;


}
