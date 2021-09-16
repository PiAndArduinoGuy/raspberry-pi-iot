package piandarduinoguy.raspberrypi.securitymsrv.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import piandarduinoguy.raspberrypi.securitymsrv.domain.SecurityConfig;

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

    public void saveSecurityConfig(SecurityConfig securityConfig) throws IOException {
        objectMapper.writeValue(new File(securityConfigFileLocation), securityConfig);
    }
}
