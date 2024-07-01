package nl.xs4all.pvbemmel.letour;

import java.util.*;

public class TeamNameId {

  public static String getTeamNameId(String teamName) {
    ArrayList<Character> chars = new ArrayList<>();
    for(int i=0; i<teamName.length(); ++i) {
      if(Character.isAlphabetic(teamName.codePointAt(i))) {
        chars.add(teamName.charAt(i));
      }
    }
    char[] cs = new char[chars.size()];
    for(int i=0; i<cs.length; ++i) {
      cs[i] = chars.get(i);
    }
    String rv = new String(cs);
    return rv;
  }
}
