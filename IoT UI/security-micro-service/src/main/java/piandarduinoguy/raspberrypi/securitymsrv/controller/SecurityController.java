package piandarduinoguy.raspberrypi.securitymsrv.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import piandarduinoguy.raspberrypi.securitymsrv.domain.SecurityConfig;

public interface SecurityController {

    @PutMapping("update/security-config")
    default ResponseEntity<Void> updateSecurityConfig(@RequestBody SecurityConfig securityConfig){
        return new ResponseEntity<>(HttpStatus.NOT_IMPLEMENTED);
    }

}
