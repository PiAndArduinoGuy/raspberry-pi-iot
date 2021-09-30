package piandarduinoguy.raspberrypi.securitymsrv.validation;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import piandarduinoguy.raspberrypi.securitymsrv.exception.ImageFileException;

import java.io.File;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ExtendWith(SpringExtension.class)
class ValidationUtilUnitTests {

    @Test
    @DisplayName("Given an annotated image does not exist " +
            "when getAnnotatedImage method called " +
            "then the exception thrown.")
    void canThrowExceptionIfAnnotatedImageDoesNotExist() {
        File imageFile = new File("a/location/to/an/image/that/does/not/exist");
        assertThatThrownBy(() -> ValidationUtil.validateImageFile(imageFile))
                .isInstanceOf(ImageFileException.class)
                .hasMessage("The File a/location/to/an/image/that/does/not/exist does not exist.");
    }
}
