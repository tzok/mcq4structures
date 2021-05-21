package pl.poznan.put.ws.controlleradvices;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import pl.poznan.put.ws.exceptions.NoGitPropertiesException;

@ControllerAdvice
public class NoGitPropertiesExceptionAdvice {

  @ResponseBody
  @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
  @ExceptionHandler(NoGitPropertiesException.class)
  String NoGitPropertiesExceptionHandler(NoGitPropertiesException noGitPropertiesException) {
    return noGitPropertiesException.getMessage();
  }
}
