package quintin.raspberrypi.control_hub.exception;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class Problem {
    private String title;
    private Integer status;
    private String detail;
}
