package piandarduinoguy.raspberrypi.securitymsrv.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import piandarduinoguy.raspberrypi.securitymsrv.domain.SecurityConfig;
import piandarduinoguy.raspberrypi.securitymsrv.domain.SecurityState;
import piandarduinoguy.raspberrypi.securitymsrv.domain.SecurityStatus;

import java.io.File;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@TestPropertySource("classpath:application-test.properties")
class SecurityConfigServiceUnitTest {
    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private SecurityConfigService securityConfigService;

    @Value("${security-config-file-location}")
    private String securityConfigFileLocation;

    private File testSecurityConfigFile;

    @BeforeEach
    public void setUp() {
        this.testSecurityConfigFile = new File(securityConfigFileLocation);
    }

    @Test
    @DisplayName("Given a security config file exists with security status BREACHED and security state ARMED, " +
            "when getSecurityConfig called, " +
            "then returned with expected domain object attributes set.")
    void canGetSecurityConfig() throws Exception {

        objectMapper.writeValue(testSecurityConfigFile, new SecurityConfig(SecurityStatus.BREACHED, SecurityState.ARMED));

        SecurityConfig securityConfig = securityConfigService.getSecurityConfig();

        assertThat(securityConfig.getSecurityStatus()).isEqualTo(SecurityStatus.BREACHED);
        assertThat(securityConfig.getSecurityState()).isEqualTo(SecurityState.ARMED);
    }

    @Test
    @DisplayName("Given a security config domain object with attributes BREACHED for security status and ARMED for security state, " +
            "when saveSecurityConfig called, " +
            "then save a security_config.json file with fields set accordingly.")
    void canSaveSecurityConfig() throws Exception {
        SecurityConfig securityConfig = new SecurityConfig(SecurityStatus.BREACHED, SecurityState.ARMED);

        securityConfigService.saveSecurityConfig(securityConfig);

        assertThatExpectedSecurityConfigJsonFileSaved();
    }

    @AfterEach
    void tearDown() {
        this.testSecurityConfigFile.delete();
    }

    private void assertThatExpectedSecurityConfigJsonFileSaved() throws Exception {
        SecurityConfig securityConfig = objectMapper.readValue(testSecurityConfigFile, SecurityConfig.class);
        assertThat(securityConfig.getSecurityStatus()).isEqualTo(SecurityStatus.BREACHED);
        assertThat(securityConfig.getSecurityState()).isEqualTo(SecurityState.ARMED);
    }

}
