package hara.lib.graal;

import org.graalvm.polyglot.HostAccess;

public class GraalGuestException extends RuntimeException {
  @HostAccess.Export public final String message;
  @HostAccess.Export public String stack;

  @HostAccess.Export
  public GraalGuestException(String message) {
    super(message);
    this.message = message;
    this.setStackTrace();
  }

  @HostAccess.Export
  public GraalGuestException(String message, Throwable cause) {
    super(message, cause);
    this.message = cause.getMessage();
    this.setStackTrace();
  }

  @HostAccess.Export
  public GraalGuestException(Throwable cause) {
    super(cause);
    this.message = cause.getMessage();
    this.setStackTrace();
  }

  private void setStackTrace() {
    StringBuffer buffer = new StringBuffer();
    for (StackTraceElement e : super.getStackTrace()) {
      buffer.append(e.toString());
      buffer.append("<br/>\n");
    }
    stack = buffer.toString();
  }
}
