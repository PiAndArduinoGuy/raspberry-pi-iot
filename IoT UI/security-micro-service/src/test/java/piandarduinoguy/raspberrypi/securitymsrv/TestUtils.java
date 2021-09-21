package piandarduinoguy.raspberrypi.securitymsrv;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.stereotype.Component;
import piandarduinoguy.raspberrypi.securitymsrv.domain.SecurityConfig;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

@Component
public class TestUtils {
    @Autowired
    private ObjectMapper objectMapper;

    @Value("${resources.base.location}")
    private String resourcesBaseLocation;

    public File testSecurityConfigFile;


    @PostConstruct
    public void createTestSecurityConfigFile() {
        this.testSecurityConfigFile = new File(resourcesBaseLocation + "security_config.json");
    }

    public void assertThatExpectedSecurityConfigJsonFileSaved(SecurityConfig expectedSecurityConfig) throws Exception {
        SecurityConfig securityConfig = objectMapper.readValue(testSecurityConfigFile, SecurityConfig.class);
        assertThat(securityConfig.getSecurityStatus()).isEqualTo(expectedSecurityConfig.getSecurityStatus());
        assertThat(securityConfig.getSecurityState()).isEqualTo(expectedSecurityConfig.getSecurityState());
    }

    public void deleteSecurityConfigFile() {
        this.testSecurityConfigFile.delete();
    }

    public void createSecurityConfigFile(SecurityConfig securityConfig) throws IOException {
        objectMapper.writeValue(testSecurityConfigFile, securityConfig);
    }

    public void deleteUploadedFileIfExists(){
        File savedTestImage = new File("src/test/resources/temp_test_new_capture.jpeg");
        if (savedTestImage.exists()){
            savedTestImage.delete();
        }
    }

    public static MockMultipartFile createMockMultipartFile() throws Exception {
        return new MockMultipartFile(
                "image",
                "test_new_capture.jpeg",
                "multipart/form-data",
                new FileInputStream("src/test/resources/test_new_capture.jpeg"));
    }

    public static void assertThatExpectedImageUploaded(MockMultipartFile image) throws Exception{
        File expectedSavedFile = new File("src/test/resources/temp_test_new_capture.jpeg");
        assertThat(expectedSavedFile).exists();
        byte[] expectedImageByteData = image.getBytes();
        byte[] savedImageByteData = FileUtils.readFileToByteArray(expectedSavedFile);
        assertThat(expectedImageByteData).isEqualTo(savedImageByteData);
    }
}
