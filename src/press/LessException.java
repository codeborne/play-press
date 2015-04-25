package press;

public class LessException extends Exception {
  private String filename;

  public LessException(String filename) {
    super(filename);
    this.filename = filename;
  }

  public LessException(String message, Throwable cause) {
    super(message, cause);
  }

  public String getFilename() {
    return filename;
  }
}
