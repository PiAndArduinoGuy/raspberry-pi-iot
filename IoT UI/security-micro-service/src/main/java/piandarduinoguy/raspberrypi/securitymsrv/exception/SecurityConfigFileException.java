package piandarduinoguy.raspberrypi.securitymsrv.exception;

import piandarduinoguy.raspberrypi.securitymsrv.domain.SecurityConfig;

public class SecurityConfigFileException extends RuntimeException {
    public SecurityConfigFileException(String message) {
        super(message);
    }
}
