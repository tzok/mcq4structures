package pl.poznan.put.comparison.exception;

public class IncomparableStructuresException extends Exception {
  public IncomparableStructuresException(String message, Throwable cause) {
    super(message, cause);
  }

  public IncomparableStructuresException(String message) {
    super(message);
  }
}
