package piandarduinoguy.raspberrypi.securitymsrv.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class SecurityConfig {
    private SecurityStatus securityStatus;
    private SecurityState securityState;
}
