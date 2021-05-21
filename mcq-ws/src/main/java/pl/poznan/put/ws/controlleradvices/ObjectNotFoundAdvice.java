package pl.poznan.put.ws.controlleradvices;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import pl.poznan.put.ws.exceptions.ObjectNotFoundException;


@ControllerAdvice
public class ObjectNotFoundAdvice {

  @ResponseBody
  @ResponseStatus(HttpStatus.NOT_FOUND)
  @ExceptionHandler(ObjectNotFoundException.class)
  String objectNotFoundHandler(ObjectNotFoundException ex) {
        return ex.getMessage();
    }
}
