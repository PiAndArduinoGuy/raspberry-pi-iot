package piandarduinoguy.raspberrypi.securitymsrv.validation;

import org.springframework.http.HttpStatus;
import piandarduinoguy.raspberrypi.securitymsrv.exception.ImageFileException;

import java.io.File;

public class ValidationUtil {
    public static void validateImageFile(File imageFile){
        if (!imageFile.exists()){
            throw new ImageFileException(String.format("The File %s does not exist.", imageFile.getPath()), HttpStatus.NOT_FOUND);
        }
    }
}
