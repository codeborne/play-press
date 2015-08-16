package press;

public class LessException extends RuntimeException {
  private final String filename;

  public LessException(String message, String filename) {
    super(message);
    this.filename = filename;
  }

  public LessException(String message, Throwable cause, String filename) {
    super(message, cause);
    this.filename = filename;
  }

  public String getFilename() {
    return filename;
  }
}
