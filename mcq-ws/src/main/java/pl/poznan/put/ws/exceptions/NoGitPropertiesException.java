package pl.poznan.put.ws.exceptions;

public class NoGitPropertiesException extends RuntimeException {
  public NoGitPropertiesException() {
    super("File git.properties has not been found!");
  }
}
