package piandarduinoguy.raspberrypi.securitymsrv.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.TestPropertySource;
import org.springframework.web.multipart.MultipartFile;
import piandarduinoguy.raspberrypi.securitymsrv.TestUtils;
import piandarduinoguy.raspberrypi.securitymsrv.domain.SecurityConfig;
import piandarduinoguy.raspberrypi.securitymsrv.domain.SecurityState;
import piandarduinoguy.raspberrypi.securitymsrv.domain.SecurityStatus;
import piandarduinoguy.raspberrypi.securitymsrv.exception.ImageFileException;
import piandarduinoguy.raspberrypi.securitymsrv.exception.SecurityConfigFileException;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;

@SpringBootTest
@TestPropertySource("classpath:application-test.properties")
class SecurityServiceUnitTest {
    @SpyBean
    private ObjectMapper objectMapper;

    @Autowired
    private SecurityService securityService;

    @Autowired
    private TestUtils testUtils;

    @Value("${resources.base.location}")
    private String resourcesBaseLocation;

    @Value("${new-capture.file-name}")
    private String newCaptureFileName;

    @Value("${new-capture.annotated.file-name}")
    private String newCaptureAnnotatedFileName;


    @Test
    @DisplayName("Given a security config file exists with security status BREACHED and security state ARMED, " +
            "when getSecurityConfig called, " +
            "then returned with expected domain object attributes set.")
    void canGetSecurityConfig() throws Exception {
        testUtils.createSecurityConfigFile(new SecurityConfig(SecurityStatus.BREACHED, SecurityState.ARMED));

        SecurityConfig securityConfig = securityService.getSecurityConfig();

        assertThat(securityConfig.getSecurityStatus()).isEqualTo(SecurityStatus.BREACHED);
        assertThat(securityConfig.getSecurityState()).isEqualTo(SecurityState.ARMED);
    }

    @Test
    @DisplayName("Given a security config domain object with attributes BREACHED for security status and ARMED for security state, " +
            "when saveSecurityConfig called, " +
            "then save a security_config.json file with fields set accordingly.")
    void canSaveSecurityConfig() throws Exception {
        SecurityConfig securityConfig = new SecurityConfig(SecurityStatus.BREACHED, SecurityState.ARMED);

        securityService.saveSecurityConfig(securityConfig);

        testUtils.assertThatExpectedSecurityConfigJsonFileSaved(securityConfig);
    }

    @Test
    @DisplayName("Given object mapper throws an IO exception " +
            "when saveSecurityConfig called " +
            "then throw SecurityConfigFileSaveException with expected message.")
    void canThrowSecurityConfigFileExceptionIfObjectMapperWriteValueMethodThrowsIOException() throws Exception {
        doThrow(new IOException("An IO exception has occurred.")).when(objectMapper).writeValue(any(File.class), any(SecurityConfig.class));

        SecurityConfig securityConfig = new SecurityConfig(SecurityStatus.BREACHED, SecurityState.ARMED);

        assertThatThrownBy(() -> securityService.saveSecurityConfig(securityConfig))
                .isInstanceOf(SecurityConfigFileException.class)
                .hasMessage("Could not save the security config file object SecurityConfig(securityStatus=BREACHED, securityState=ARMED) to src/test/resources/application/security_config.json due to an IOException with message \"An IO exception has occurred.\".");

    }

    @Test
    @DisplayName("Given object mapper throws an IO exception " +
            "when saveSecurityConfig called " +
            "then throw SecurityConfigFileSaveException with expected message.")
    void canThrowSecurityConfigFileExceptionIfObjectMapperReadMethodThrowsIOException() throws Exception {
        doThrow(new IOException("An IO exception has occurred.")).when(objectMapper).readValue(eq(testUtils.testSecurityConfigFile), eq(SecurityConfig.class));
        assertThatThrownBy(() -> securityService.getSecurityConfig())
                .isInstanceOf(SecurityConfigFileException.class)
                .hasMessage("Could not retrieve security config due to an IOException with message \"An IO exception has occurred.\".");

    }

    @Test
    @DisplayName("Given a valid multipart file " +
            "when saveImage called " +
            "then image is saved as expected.")
    void canSaveUploadedImageToBeProcessed() throws Exception {
        MockMultipartFile image = TestUtils.createMockMultipartFile();

        securityService.saveImage(image);

        testUtils.assertThatExpectedImageUploaded(image);
    }

    @Disabled("Requires mocking of static method FileUtils.writeByteArrayToFile, use powermock for this then remove this annotation.")
    @Test
    @DisplayName("Given FileUtils.writeByteArrayToFile method throws an IOException " +
            "when saveImage called " +
            "then an ImageFileException is thrown.")
    void canThrowImageFileExceptionWhenSaveImageCalled() throws Exception {
        MultipartFile image = new MockMultipartFile("test_new_capture.jpeg", new FileInputStream("directory/that/does/not/exists/test_new_capture.jpeg"));

        assertThatThrownBy(() -> {
            securityService.saveImage(image);
        }).isInstanceOf(ImageFileException.class).hasMessage("An IOException occurred trying to save the image.");

    }

    @Test
    @DisplayName("Given an annotated image exists " +
            "when getAnnotatedImage method called " +
            "then the expected is returned as base64 encoded image.")
    void canReturnAnnotatedImage() throws Exception {
        testUtils.createExpectedAnnotatedImageFile();

        String base64AnnotatedImage = securityService.getBase64AnnotatedImage();

        assertThat(testUtils.getExpectedBase64EncodedAnnotatedImage()).isEqualToIgnoringCase(base64AnnotatedImage);

        testUtils.deleteAnnotatedImage();
    }

    @Disabled("Requires mocking of static Base64.encode method, use powermock for this then remove this annotation.")
    @Test
    @DisplayName("Given Base64.encode method throws an IOException " +
            "when getBase64AnnotatedImage called " +
            "then an ImageFileException is thrown.")
    void canThrowImageFileExceptionWhenGetBase64AnnotatedImageCalled() {
        assertThatThrownBy(() -> securityService.getBase64AnnotatedImage())
                .isInstanceOf(ImageFileException.class)
                .hasMessage("The File src/test/resources/application/test_new_capture_annotated.jpeg does not exist.");
    }

    @AfterEach
    void tearDown() {
        this.testUtils.deleteSecurityConfigFile();
        this.testUtils.deleteUploadedFileIfExists();
    }

}
