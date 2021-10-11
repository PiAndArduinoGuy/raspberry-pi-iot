package piandarduinoguy.raspberrypi.securitymsrv.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import piandarduinoguy.raspberrypi.securitymsrv.domain.SecurityConfig;
import piandarduinoguy.raspberrypi.securitymsrv.service.SecurityService;

@RestController
public class SecurityControllerImpl implements SecurityController {
    @Autowired
    private SecurityService securityService;

    public ResponseEntity<Void> updateSecurityConfig(SecurityConfig securityConfig) {
        securityService.saveSecurityConfig(securityConfig);

        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @Override
    public ResponseEntity<SecurityConfig> getSecurityConfig() {
        return new ResponseEntity<>(securityService.getSecurityConfig(), HttpStatus.OK);
    }

    @Override
    public ResponseEntity<Void> detectObjectsInImage(MultipartFile image) {
        securityService.saveImage(image);
        // TODO: perform the object detection
        return new ResponseEntity<>(HttpStatus.ACCEPTED);
    }

    @Override
    public ResponseEntity<String> getAnnotatedImage() {
        return new ResponseEntity(securityService.getBase64AnnotatedImage(), HttpStatus.OK);
    }
}