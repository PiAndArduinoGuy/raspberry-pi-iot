package piandarduinoguy.raspberrypi.securitymsrv.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.Mapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import piandarduinoguy.raspberrypi.securitymsrv.domain.SecurityConfig;

public interface SecurityController {

    @PutMapping("update/security-config")
    default ResponseEntity<Void> updateSecurityConfig(@RequestBody SecurityConfig securityConfig){
        return new ResponseEntity<>(HttpStatus.NOT_IMPLEMENTED);
    }

    @PostMapping(value = "object-detect")
    default ResponseEntity<Void> detectObjectsInImage(@RequestParam("image") MultipartFile image){
        return new ResponseEntity<>(HttpStatus.NOT_IMPLEMENTED);
    }

    @GetMapping("security-config")
    default ResponseEntity<SecurityConfig> getSecurityConfig(){
        return new ResponseEntity<>(HttpStatus.NOT_IMPLEMENTED);
    }

    @GetMapping("annotated-image")
    default ResponseEntity<String> getAnnotatedImage(){
        return new ResponseEntity<>(HttpStatus.NOT_IMPLEMENTED);
    }

}
