package piandarduinoguy.raspberrypi.securitymsrv.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import piandarduinoguy.raspberrypi.securitymsrv.domain.SecurityConfig;
import piandarduinoguy.raspberrypi.securitymsrv.exception.ImageFileException;
import piandarduinoguy.raspberrypi.securitymsrv.exception.SecurityConfigFileException;

import java.io.File;
import java.io.IOException;

@Service
public class SecurityConfigService {
    private ObjectMapper objectMapper;

    @Value("${resources.base.location}")
    private String resourcesBaseLocation;

    @Autowired
    public SecurityConfigService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public SecurityConfig getSecurityConfig() {
        try {
            return objectMapper.readValue(new File(resourcesBaseLocation + "security_config.json"), SecurityConfig.class);
        } catch (IOException ioException) {
            throw new SecurityConfigFileException(String.format(
                    "Could not retrieve security config due to an IOException with message \"%s\".",
                    ioException.getMessage()));
        }
    }

    public void saveSecurityConfig(SecurityConfig securityConfig) {
        try {
            objectMapper.writeValue(new File(resourcesBaseLocation + "security_config.json"), securityConfig);
        } catch (IOException ioException) {
            throw new SecurityConfigFileException(String.format(
                    "Could not save the security config file object %s to %s due to an IOException with message \"%s\".",
                    securityConfig,
                    resourcesBaseLocation + "security_config.json",
                    ioException.getMessage()));
        }
    }

    public void saveImage(MultipartFile image) {
        try {
            byte[] imageData = image.getBytes();
            String uploadedImageFileName = String.format("/temp_%s", image.getOriginalFilename());
            File imageFile = new File(resourcesBaseLocation + uploadedImageFileName);
            FileUtils.writeByteArrayToFile(imageFile, imageData);
        } catch (IOException ioException) {
            throw new ImageFileException(String.format(
                    "The image %s could not be saved to the directory %s. An IOException was thrown with message \"%s\".",
                    image.getName(),
                    resourcesBaseLocation,
                    ioException.getMessage()));
        }
    }
}
