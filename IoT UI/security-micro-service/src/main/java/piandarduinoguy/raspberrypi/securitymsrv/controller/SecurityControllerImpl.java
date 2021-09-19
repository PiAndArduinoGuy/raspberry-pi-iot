package piandarduinoguy.raspberrypi.securitymsrv.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import piandarduinoguy.raspberrypi.securitymsrv.domain.SecurityConfig;
import piandarduinoguy.raspberrypi.securitymsrv.service.SecurityConfigService;

@RestController
public class SecurityControllerImpl implements SecurityController {
    @Autowired
    private SecurityConfigService securityConfigService;

    public ResponseEntity<Void> updateSecurityConfig(SecurityConfig securityConfig) {
        securityConfigService.saveSecurityConfig(securityConfig);

        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @Override
    public ResponseEntity<SecurityConfig> getSecurityConfig() {
        return new ResponseEntity<>(securityConfigService.getSecurityConfig(), HttpStatus.OK);
    }
}
