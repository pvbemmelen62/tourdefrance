package nl.xs4all.pvbemmel.letour;

public class StringUtil {

  public static boolean isUpperCase(String s) {
    for (int i = 0; i < s.length(); i++) {
      if (Character.isLowerCase(s.charAt(i))) {
        return false;
      }
    }
    return true;
  }
}
