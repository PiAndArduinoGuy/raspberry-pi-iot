package piandarduinoguy.raspberrypi.securitymsrv.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.org.apache.xerces.internal.impl.dv.util.Base64;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import piandarduinoguy.raspberrypi.securitymsrv.domain.SecurityConfig;
import piandarduinoguy.raspberrypi.securitymsrv.exception.ImageFileException;
import piandarduinoguy.raspberrypi.securitymsrv.exception.SecurityConfigFileException;
import piandarduinoguy.raspberrypi.securitymsrv.validation.ValidationUtil;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

@Service
public class SecurityService {
    private ObjectMapper objectMapper;

    @Value("${resources.base.location}")
    private String resourcesBaseLocation;

    @Value("${new-capture.file-name}")
    private String newCaptureFileName;

    @Value("${new-capture.annotated.file-name}")
    private String newCaptureAnnotatedFileName;

    @Autowired
    public SecurityService(ObjectMapper objectMapper) {
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
            File imageFile = new File(resourcesBaseLocation + newCaptureFileName);
            FileUtils.writeByteArrayToFile(imageFile, imageData);
        } catch (IOException ioException) {
            throw new ImageFileException(String.format(
                    "The image %s could not be saved to the directory %s. An IOException was thrown with message \"%s\".",
                    image.getName(),
                    resourcesBaseLocation,
                    ioException.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public String getBase64AnnotatedImage() {
        File annotatedImageFile = new File(resourcesBaseLocation + newCaptureAnnotatedFileName);
        ValidationUtil.validateImageFile(annotatedImageFile);
        try {
            return Base64.encode(FileUtils.readFileToByteArray(annotatedImageFile));
        } catch (IOException ioException) {
            throw new ImageFileException(String.format(
                    "The image %s could not be encoded to base64 string due to an IOException being thrown with message \"%s\".",
                    annotatedImageFile.getName(),
                    ioException.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
