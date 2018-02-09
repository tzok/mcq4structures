package pl.poznan.put.mcq.cli;

/** Created by tzok on 05.01.16. */
@Deprecated
public class McqProcessingException extends Exception {
  public McqProcessingException(Throwable cause) {
    super(cause);
  }

  public McqProcessingException() {
    super();
  }

  public McqProcessingException(String message) {
    super(message);
  }

  public McqProcessingException(String message, Throwable cause) {
    super(message, cause);
  }
}
