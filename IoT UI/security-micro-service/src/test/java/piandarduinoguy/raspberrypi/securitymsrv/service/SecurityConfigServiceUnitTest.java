package piandarduinoguy.raspberrypi.securitymsrv.service;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Spy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.test.context.TestPropertySource;
import piandarduinoguy.raspberrypi.securitymsrv.TestUtils;
import piandarduinoguy.raspberrypi.securitymsrv.domain.SecurityConfig;
import piandarduinoguy.raspberrypi.securitymsrv.domain.SecurityState;
import piandarduinoguy.raspberrypi.securitymsrv.domain.SecurityStatus;
import piandarduinoguy.raspberrypi.securitymsrv.exception.SecurityConfigFileException;

import java.io.File;
import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

@SpringBootTest
@TestPropertySource("classpath:application-test.properties")
class SecurityConfigServiceUnitTest {
    @SpyBean
    private ObjectMapper objectMapper;

    @Autowired
    private SecurityConfigService securityConfigService;

    @Autowired
    private TestUtils testUtils;


    @Test
    @DisplayName("Given a security config file exists with security status BREACHED and security state ARMED, " +
            "when getSecurityConfig called, " +
            "then returned with expected domain object attributes set.")
    void canGetSecurityConfig() throws Exception {
        testUtils.createSecurityConfigFile(new SecurityConfig(SecurityStatus.BREACHED, SecurityState.ARMED));

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

        testUtils.assertThatExpectedSecurityConfigJsonFileSaved(securityConfig);
    }

    @Test
    @DisplayName("Given object mapper throws an IO exception " +
            "when saveSecurityConfig called " +
            "then throw SecurityConfigFileSaveException with expected message.")
    void canThrowSecurityConfigFileExceptionIfObjectMapperWriteValueMethodThrowsIOException() throws Exception {
        doThrow(new IOException("An IO exception has occurred.")).when(objectMapper).writeValue(any(File.class), any(SecurityConfig.class));

        SecurityConfig securityConfig = new SecurityConfig(SecurityStatus.BREACHED, SecurityState.ARMED);

        assertThatThrownBy(() -> securityConfigService.saveSecurityConfig(securityConfig))
                .isInstanceOf(SecurityConfigFileException.class)
                .hasMessage("Could not save the security config file object SecurityConfig(securityStatus=BREACHED, securityState=ARMED) to src/test/resources/security_config.json due to an IOException with message \"An IO exception has occurred.\".");

    }

    @Test
    @DisplayName("Given object mapper throws an IO exception " +
            "when saveSecurityConfig called " +
            "then throw SecurityConfigFileSaveException with expected message.")
    void canThrowSecurityConfigFileExceptionIfObjectMapperReadMethodThrowsIOException() throws Exception {
        doThrow(new IOException("An IO exception has occurred.")).when(objectMapper).readValue(eq(testUtils.testSecurityConfigFile), eq(SecurityConfig.class));
        assertThatThrownBy(() -> securityConfigService.getSecurityConfig())
                .isInstanceOf(SecurityConfigFileException.class)
                .hasMessage("Could not retrieve security config due to an IOException with message \"An IO exception has occurred.\".");

    }

    @AfterEach
    void tearDown() {
        this.testUtils.deleteSecurityConfigFile();
    }

}
