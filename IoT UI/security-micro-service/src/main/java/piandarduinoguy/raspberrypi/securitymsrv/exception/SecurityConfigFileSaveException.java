package piandarduinoguy.raspberrypi.securitymsrv.exception;

import piandarduinoguy.raspberrypi.securitymsrv.domain.SecurityConfig;

public class SecurityConfigFileSaveException extends RuntimeException {
    public SecurityConfigFileSaveException(String message) {
        super(message);
    }
}
