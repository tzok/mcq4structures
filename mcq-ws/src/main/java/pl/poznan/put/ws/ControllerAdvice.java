package pl.poznan.put.ws;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import pl.poznan.put.ws.exceptions.NoGitPropertiesException;
import pl.poznan.put.ws.exceptions.ObjectNotFoundException;
import pl.poznan.put.ws.exceptions.PathVariableException;

@org.springframework.web.bind.annotation.ControllerAdvice
public class ControllerAdvice {

  @ResponseBody
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  @ExceptionHandler(IllegalArgumentException.class)
  private String illegalArgumentExceptionHandler(
      IllegalArgumentException illegalArgumentException) {
    return illegalArgumentException.getMessage();
  }


  @ResponseBody
  @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
  @ExceptionHandler(NoGitPropertiesException.class)
  private String NoGitPropertiesExceptionHandler(
      NoGitPropertiesException noGitPropertiesException) {
    return noGitPropertiesException.getMessage();
  }

  @ResponseBody
  @ResponseStatus(HttpStatus.NOT_FOUND)
  @ExceptionHandler(ObjectNotFoundException.class)
  private String objectNotFoundHandler(ObjectNotFoundException objectNotFoundException) {
    return objectNotFoundException.getMessage();
  }

  @ResponseBody
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  @ExceptionHandler(PathVariableException.class)
  private String pathInputExceptionHandler(PathVariableException pathVariableException) {
    return pathVariableException.getMessage();
  }

  @ResponseBody
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  @ExceptionHandler(MethodArgumentNotValidException.class)
  private String methodArgumentNotValidExceptionHandler(
      MethodArgumentNotValidException methodArgumentNotValidException) {
    return methodArgumentNotValidException.getMessage();
  }
}
