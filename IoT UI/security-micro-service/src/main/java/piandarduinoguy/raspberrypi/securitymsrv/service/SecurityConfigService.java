package piandarduinoguy.raspberrypi.securitymsrv.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import piandarduinoguy.raspberrypi.securitymsrv.domain.SecurityConfig;
import piandarduinoguy.raspberrypi.securitymsrv.exception.SecurityConfigFileSaveException;

import java.io.File;
import java.io.IOException;

@Service
public class SecurityConfigService {
    @Autowired
    private ObjectMapper objectMapper;

    @Value("${security-config-file-location}")
    private String securityConfigFileLocation;

    public SecurityConfig getSecurityConfig() throws IOException {
        return objectMapper.readValue(new File(securityConfigFileLocation), SecurityConfig.class);
    }

    public void saveSecurityConfig(SecurityConfig securityConfig) {
        try {
            objectMapper.writeValue(new File(securityConfigFileLocation), securityConfig);
        } catch (IOException ioException) {
            throw new SecurityConfigFileSaveException(String.format(
                    "Could not save the security config file object %s to %s due to an IOException with message \"%s\".",
                    securityConfig,
                    securityConfigFileLocation,
                    ioException.getMessage()));
        }
    }
}
