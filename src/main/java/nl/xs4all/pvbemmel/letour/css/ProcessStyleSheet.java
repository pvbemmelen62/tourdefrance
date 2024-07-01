package nl.xs4all.pvbemmel.letour.css;
import java.io.*;
import java.util.*;

import org.w3c.css.sac.*;
import org.w3c.dom.css.*;

import com.steadystate.css.parser.*;

import nl.xs4all.pvbemmel.letour.*;

public class ProcessStyleSheet {

  public static String CssFileName = "styles.20170719.css";
  public static String CssFileName1 = "jersey-img-positions.20170719.css";
  public static String CssPath0 = "data/" + CssFileName;
  public static String CssPath1 = "data/" + CssFileName1;
  private static String CssPath0IsMissingMsg =
   "Error: File " + CssPath0 + " is missing.\n"
  +"\n"
  +"Follow instructions below, to create " + CssPath0 + " .\n"
  +"\n"
  +"On letour.fr, the team jersey images are not separate files, but all part\n"
  +"of one large image.\n"
  +"The css for each jersey specifies the offset of the jersey within the one\n"
  +"large image.\n"
  +"\n"
  +"In Firefox: File->Open Location->http://www.letour.fr\n"
  +"Click \"TEAMS\"\n"
  +"Open Firebug window, click the select icon, click on a team jersey,\n"
  +"and from the shown html element, find the css style declarations that\n"
  +"specify the background image;\n"
  +"For each team there will be a background-position spec; e.g. for team\n"
  +" WANTY - GROUPE GOBERT there will be\n"
  +"\n"
  +" .jerseyTeam-wgg {\n"
  +"   background-position: 0 -2667px;\n"
  +" }\n"
  +"\n"
  +"Save the stylesheet containing these specs, as file " + CssPath0 + " .\n"
  ;
  public static void main(String[] args) throws IOException {
    processStyleSheet();
  }
  public static void processStyleSheet() throws IOException {
    File file = new File(CssPath0);
    if(!file.exists()) {
      System.out.println(CssPath0IsMissingMsg);
      System.exit(1);
    }
    InputSource source = new InputSource(new FileReader(CssPath0));
    CSSOMParser parser = new CSSOMParser(new SACParserCSS3());
    ErrorHandler errorHandler = new MyErrorHandler();
    parser.setErrorHandler(errorHandler);

    CSSStyleSheet sheet = parser.parseStyleSheet(source, null, null);
    CSSRuleList rules = sheet.getCssRules();
    try (BufferedWriter bw = new BufferedWriter(new FileWriter(CssPath1))) {
      for (int i = 0; i < rules.getLength(); i++) {
        final CSSRule rule = rules.item(i);

        if(rule.getType()==CSSRule.STYLE_RULE) {
          String text = rule.getCssText();
          if(text.matches(".*jerseyTeam--[a-z]{3}.*")) {
            bw.write(text);
            bw.newLine();
          }
//          else if(text.contains(".subnav__teams__name")
//            || text.contains("subnav__teams__link")) {
//            bw.write(text);
//            bw.newLine();
//          }
//          else {
//            Pattern pattern = Pattern.compile(
//                "(.*background-image: url\\()(.*)(\\);.*)");
//            Matcher matcher = pattern.matcher(text);
//            if(matcher.matches()) {
//              String s = matcher.group(2);
//              String t = s.substring(s.lastIndexOf("/")+1, s.length());
//              String u = matcher.group(1) + t + matcher.group(3);
//              bw.write(u);
//              bw.newLine();
//            }
//            else if(text.contains(".jerseyTeam")) {
//              bw.write(text);
//              bw.newLine();
//            }
//          }
        }
      }
    }
  }
  public static HashMap<String,Integer> getImgPositionMap()
      throws IOException {
    File file = new File(CssPath1);
    if(!file.exists()) {
      processStyleSheet();
    }
    HashMap<String,Integer> map = new HashMap<>();
    List<String> lines = FileUtil.readFileLines(CssPath1);
    // Code below assumes that each line contains one css spec, like this:
    // .jerseyTeam--fdj { background-position: 0 -1016px }
    for(String line : lines) {
      if(line.trim().isEmpty()) {
        continue;
      }
      if(!line.matches(".*jerseyTeam--[a-z]{3}.*")) {
        continue;
      }
      line = line.trim();
      int index = ".jerseyTeam--".length();
      String abbrev = line.substring(index, index+3);
      String[] words = line.split("\\s+");
      String word = words[words.length-2];
      if(word.endsWith("px")) {
        word = word.substring(0, word.indexOf("px"));
      }
      Integer y = Integer.valueOf(word);
      map.put(abbrev, y);
    }
    return map;
  }
  public static class Test {
    public static void main(String[] args) throws IOException {
      HashMap<String,Integer> map = getImgPositionMap();
      System.out.println(map.toString());
    }
  }
}
