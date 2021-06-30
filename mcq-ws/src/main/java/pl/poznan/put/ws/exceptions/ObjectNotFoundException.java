package pl.poznan.put.ws.exceptions;

public class ObjectNotFoundException extends RuntimeException {
  public ObjectNotFoundException(String id, Class<?> className) {
    super(className.getSimpleName() + " with id: " + id + " has not been found!");
  }
}
