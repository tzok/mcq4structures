package pl.poznan.put.ws.exceptions;

public class PathVariableException extends RuntimeException {
  public PathVariableException(String pathName, String pathValue, String problem) {
    super(
        "Path variable "
            + pathName
            + " with given value "
            + pathValue
            + " encountered exception: "
            + problem);
  }
}
