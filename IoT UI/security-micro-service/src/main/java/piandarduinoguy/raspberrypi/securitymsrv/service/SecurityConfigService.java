package piandarduinoguy.raspberrypi.securitymsrv.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import piandarduinoguy.raspberrypi.securitymsrv.domain.SecurityConfig;
import piandarduinoguy.raspberrypi.securitymsrv.exception.SecurityConfigFileException;

import java.io.File;
import java.io.IOException;

@Service
public class SecurityConfigService {
    private ObjectMapper objectMapper;

    @Value("${security-config-file-location}")
    private String securityConfigFileLocation;

    @Autowired
    public SecurityConfigService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public SecurityConfig getSecurityConfig() {
        try {
            return objectMapper.readValue(new File(securityConfigFileLocation), SecurityConfig.class);
        } catch (IOException ioException) {
            throw new SecurityConfigFileException(String.format(
                    "Could not retrieve security config due to an IOException with message \"%s\".",
                    ioException.getMessage()));
        }
    }

    public void saveSecurityConfig(SecurityConfig securityConfig) {
        try {
            objectMapper.writeValue(new File(securityConfigFileLocation), securityConfig);
        } catch (IOException ioException) {
            throw new SecurityConfigFileException(String.format(
                    "Could not save the security config file object %s to %s due to an IOException with message \"%s\".",
                    securityConfig,
                    securityConfigFileLocation,
                    ioException.getMessage()));
        }
    }
}
