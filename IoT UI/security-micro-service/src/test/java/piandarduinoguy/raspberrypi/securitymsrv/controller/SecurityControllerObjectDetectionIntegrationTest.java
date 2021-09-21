package piandarduinoguy.raspberrypi.securitymsrv.controller;

import org.apache.commons.io.FileUtils;
import org.apache.tomcat.util.http.parser.MediaType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.PropertySource;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.multipart.MultipartFile;
import piandarduinoguy.raspberrypi.securitymsrv.TestUtils;
import piandarduinoguy.raspberrypi.securitymsrv.service.SecurityConfigService;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

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
    private SecurityConfigService securityConfigService;

    @Autowired
    private TestUtils testUtils;

    @DisplayName("Given a valid multipart file " +
            "when port to the /object-detect endpoint is made " +
            "then the image file gets saved at the expected location.")
    @Test
    void canUploadNewCaptureForProcessing() throws Exception {
        MockMultipartFile image = TestUtils.createMockMultipartFile();

        mockMvc.perform(
                multipart("/object-detect").file(image)).
                andExpect(status().isAccepted());

        TestUtils.assertThatExpectedImageUploaded(image);
        // assertion that we can perform detection after it has saved

        testUtils.deleteUploadedFileIfExists();
    }


}
