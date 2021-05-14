package pl.poznan.put.ws.controlleradvices;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import pl.poznan.put.ws.exceptions.PathVariableException;

@ControllerAdvice
public class PathVariableExceptionAdvice {

    @ResponseBody
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(PathVariableException.class)
    private String pathInputExceptionHandler(PathVariableException pathVariableException) {
        return pathVariableException.getMessage();
    }
}
