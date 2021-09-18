package piandarduinoguy.raspberrypi.securitymsrv;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import piandarduinoguy.raspberrypi.securitymsrv.domain.SecurityConfig;
import piandarduinoguy.raspberrypi.securitymsrv.domain.SecurityState;
import piandarduinoguy.raspberrypi.securitymsrv.domain.SecurityStatus;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

@Component
public class TestUtils {
    @Autowired
    private ObjectMapper objectMapper;

    @Value("${security-config-file-location}")
    private String securityConfigFileLocation;

    public File testSecurityConfigFile;

    @PostConstruct
    public void createTestSecurityConfigFile() {
        this.testSecurityConfigFile = new File(securityConfigFileLocation);
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
}
