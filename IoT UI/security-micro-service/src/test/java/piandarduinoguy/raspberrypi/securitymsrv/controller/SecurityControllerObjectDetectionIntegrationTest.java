package piandarduinoguy.raspberrypi.securitymsrv.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import piandarduinoguy.raspberrypi.securitymsrv.TestUtils;
import piandarduinoguy.raspberrypi.securitymsrv.service.SecurityService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource("classpath:application-test.properties")
class SecurityControllerObjectDetectionIntegrationTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private SecurityService securityService;

    @Autowired
    private TestUtils testUtils;

    @DisplayName("Given a valid multipart file " +
            "when post to the /object-detect endpoint is made " +
            "then the image file gets saved at the expected location.")
    @Test
    void canUploadNewCaptureForProcessing() throws Exception {
        MockMultipartFile image = TestUtils.createMockMultipartFile();

        mockMvc.perform(
                multipart("/object-detect").file(image)).
                andExpect(status().isAccepted());

        testUtils.assertThatExpectedImageUploaded(image);
        // TODO: assertion that we can perform detection after it has been temporarily saved

        testUtils.deleteUploadedFileIfExists();
    }
}
