package quintin.raspberrypi.pump_controller.data;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class Problem {
    private String title;
    private Integer status;
    private String detail;
}