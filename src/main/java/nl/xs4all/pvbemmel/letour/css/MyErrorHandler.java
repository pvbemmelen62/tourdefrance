package nl.xs4all.pvbemmel.letour.css;
import org.w3c.css.sac.*;

public class MyErrorHandler implements ErrorHandler {

  public void warning(CSSParseException exception) throws CSSException {
    int numLine = exception.getLineNumber();
    System.out.println("Warning (line " + numLine + "):" + exception.getMessage());
  }

  public void error(CSSParseException exception) throws CSSException {
    int numLine = exception.getLineNumber();
    System.out.println("Error (line " + numLine + "): " + exception.getMessage());
  }

  public void fatalError(CSSParseException exception) throws CSSException {
    int numLine = exception.getLineNumber();
    System.out.println("Fatal (line " + numLine + "): " + exception.getMessage());
  }
}