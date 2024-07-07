package nl.xs4all.pvbemmel.letour;

import java.io.*;
import java.nio.charset.*;
import java.util.*;

import javax.xml.parsers.*;
import javax.xml.transform.*;

import org.jdom2.*;
import org.jdom2.Attribute;
import org.jdom2.filter.*;
import org.jdom2.input.*;
import org.jdom2.xpath.*;
import org.w3c.tidy.*;
import org.xml.sax.*;

import nl.xs4all.pvbemmel.letour.css.*;

import static nl.xs4all.pvbemmel.letour.DocumentStuff.*;

public class TourDeFrance {
  private static final String nl = System.getProperty("line.separator");
  private static final String handCraftedStylesFileName = "handCrafted/jerseys.css";
  private static Tidy tidy;
  private static XPathFactory xpfac = XPathFactory.instance();
  private static HashMap<String, XPathExpression<Element>> xpeCache = new HashMap<>();
  private static HashMap<String, XPathExpression<Attribute>> xpaCache = new HashMap<>();

  private static Tidy getTidy() {
    if (tidy == null) {
      tidy = new Tidy();
      // tidy.setDocType("loose"); //: DOMBuilder().build(doc) generates
      // exception.
      tidy.setDocType("omit");
      tidy.setQuiet(true);
      tidy.setShowWarnings(false);
      tidy.setXHTML(false); // if true, then xpath expression require prefixes.
      tidy.setForceOutput(true);
      tidy.setTrimEmptyElements(false);
      tidy.setInputEncoding("utf8");
      tidy.setOutputEncoding("utf8");
    }
    return tidy;
  }
  public static void main(String[] args) throws IOException, JDOMException,
  TransformerException, ParserConfigurationException, SAXException {
  if(args.length > 0) {
    if("team-jerseys".equals(args[0])) {
      main1(args);
    }
    else if("cyclists".equals(args[0])) {
      main0(args);
    }
  }
}
/**
 * Parse teams page from www.letour.fr, and construct cyclistsIndex.html and
 * teamsIndex.html .
 * 
 * <pre>
 * In Firefox: File->Open Location->http://www.letour.fr
 * Click "TEAMS"
 * Click "START LIST"
 * Use DevTools to copy html of starters list,
 * and add surrounding <html> and <body>,
 * to 
 *     data/starters-html-only.html
 * </pre>
 */
  public static void main0(String[] args) throws IOException, JDOMException,
      TransformerException, ParserConfigurationException, SAXException {
    File file = new File("data/starters-tidy.html");
    Document doc = null;
    if (file.exists()) {
      FileInputStream fis = new FileInputStream(file);
      doc = readDocument(fis);
      fis.close();
    }
    else {
      doc = getDoc("data/starters-html-only.html");
      printDocument(doc, new FileOutputStream(file));
    }
    List<Team> teams = parseTidyAndBuildJavaObjects(doc);
    writeTeamsXml(teams);
    printTeamNamesIndex(teams);
    printTeamsIndex(teams);
    printCyclistsIndex(teams);
    printTeamsJson(teams);
    printTeamJerseys(teams);
  }
  public static void main1(String[] args) throws IOException, JDOMException,
      TransformerException, ParserConfigurationException, SAXException {
    Document doc = null;
    doc = getDoc("data/teams-jerseys-html-only.html");

    List<Element> anchors = evalAllE(doc, "//a");
    for (Element anchor : anchors) {
      String teamName = anchor.getText();
      String href = anchor.getAttributeValue("href");
      String teamNameDashed = href.substring(href.lastIndexOf("/") + 1);
      Element img = evalFirstE(anchor, "./img");
      String src = img.getAttributeValue("src");
      String teamCountry = null;
      String abbrev = null;
      if (href.matches("/[^/]+/.*")) {
        // E.g. "/en/team/INS/team-ineos"
        String[] parts = href.substring(1).split("/");
        teamCountry = parts[0];
        abbrev = Text.normalizeString(parts[2]);
      }
      else {
        // <h3 class="list__heading">
        // <a href="#">EF PRO CYCLING</a>
        // </h3>
        teamCountry = "?";
        abbrev = "?";
      }
      System.out.println("" + abbrev + " " + teamNameDashed + " " + src);
    }
  }
  private static List<Team> parseTidyAndBuildJavaObjects(Document doc) {
    // To understand the html structure, in firefox use firebug to look at the
    // HTML Dom .
    // <div class="list list--competitors">
    // <h3 class="list__heading">
    // <a href="/en/team/IGD/ineos-grenadiers">INEOS GRENADIERS</a>
    // </h3>
    // <div class="list__box">
    // <ul class="list__box__content first">
    // <li class="list__box__item"><span class="bib">1</span><span
    // class="runner"><span class="flag js-display-lazy"
    // data-class="flag--col"></span><a class="runner__link"
    // href="/en/rider/1/ineos-grenadiers/egan-bernal-gomez"> EGAN
    // BERNAL </a></span></li>
    // <li class="list__box__item"><span class="bib">2</span><span
    // class="runner"><span class="flag js-display-lazy"
    // data-class="flag--crc"></span><a class="runner__link"
    // href="/en/rider/2/ineos-grenadiers/andrey-amador"> ANDREY
    // AMADOR </a></span></li>
    // <li class="list__box__item"><span class="bib">3</span><span
    // class="runner"><span class="flag js-display-lazy"
    // data-class="flag--ecu"></span><a class="runner__link"
    // href="/en/rider/3/ineos-grenadiers/richard-carapaz"> RICHARD
    // CARAPAZ </a></span></li>
    // <li class="list__box__item"><span class="bib">4</span><span
    // class="runner"><span class="flag js-display-lazy"
    // data-class="flag--esp"></span><a class="runner__link"
    // href="/en/rider/4/ineos-grenadiers/jonathan-castroviejo-nicolas">
    // JONATHAN CASTROVIEJO </a></span></li>
    // </ul>
    // <ul class="list__box__content second">
    // <li class="list__box__item"><span class="bib">5</span><span
    // class="runner"><span class="flag js-display-lazy"
    // data-class="flag--pol"></span><a class="runner__link"
    // href="/en/rider/5/ineos-grenadiers/michal-kwiatkowski"> MICHAL
    // KWIATKOWSKI </a></span></li>
    // <li class="list__box__item"><span class="bib">6</span><span
    // class="runner"><span class="flag js-display-lazy"
    // data-class="flag--gbr"></span><a class="runner__link"
    // href="/en/rider/6/ineos-grenadiers/luke-rowe"> LUKE ROWE </a></span></li>
    // <li class="list__box__item"><span class="bib">7</span><span
    // class="runner"><span class="flag js-display-lazy"
    // data-class="flag--rus"></span><a class="runner__link"
    // href="/en/rider/7/ineos-grenadiers/pavel-sivakov"> PAVEL
    // SIVAKOV </a></span></li>
    // <li class="list__box__item"><span class="bib">8</span><span
    // class="runner"><span class="flag js-display-lazy"
    // data-class="flag--ned"></span><a class="runner__link"
    // href="/en/rider/8/ineos-grenadiers/dylan-van-baarle"> DYLAN
    // VAN BAARLE </a></span></li>
    // </ul>
    // </div>
    // <h3 class="list__heading">
    // <a href="/en/team/TJV/team-jumbo-visma">TEAM JUMBO - VISMA</a>
    // </h3>

    List<Team> teams = new ArrayList<Team>();

    List<Element> lis = evalAllE(doc, "//h3[@class=\"list__heading\"]");
    for (Element li : lis) {
      Element anchor = evalFirstE(li, "./a");
      String teamName = anchor.getText();
      String href = anchor.getAttributeValue("href");
      String teamCountry = null;
      String abbrev = null;
      if (href.matches("/[^/]+/.*")) {
        // E.g. "/en/team/INS/team-ineos"
        String[] parts = href.substring(1).split("/");
        teamCountry = parts[0];
        abbrev = Text.normalizeString(parts[2]);
      }
      else {
//        <h3 class="list__heading">
//          <a href="#">EF PRO CYCLING</a>
//        </h3>
        teamCountry = "?";
        abbrev = "?";
      }
      teamCountry = Text.normalizeString(teamCountry);

      Team team = new Team(teamName, teamCountry);
      teams.add(team);
      team.setAbbrev(abbrev);

      List<Element> riders = evalAllE(li,
          "following::div[1]//li[@class=\"list__box__item\"]");
      for (Element rider : riders) {
        Element span = evalFirstE(rider, "span[@class=\"bib\"]");
        int riderNumber = Integer.parseInt(span.getTextNormalize());
        Element runner = evalFirstE(rider, "span[@class=\"runner\"]");
        Attribute countryA = evalFirstA(runner, "span/@data-class");
        String riderCountry = null;
        for (String ss : countryA.getValue().split("\\s+")) {
          if (ss.startsWith("flag--")) {
            riderCountry = ss.substring("flag--".length());
            break;
          }
        }
        Element anchorR = evalFirstE(rider, ".//a[@class=\"runner__link\"]");
        @SuppressWarnings("unused")
        Attribute hrefR = anchorR.getAttribute("href");
        String riderName = anchorR.getTextNormalize();
        //riderName = riderName.toLowerCase();
        Cyclist cyclist = new Cyclist(riderNumber, riderName, riderCountry);
        team.addCyclist(cyclist);
      }
    }
    // XPathExpression<Element> xp =
    // xpfac.compile("//h3[@class=\"list__heading\"]",
    // Filters.element());
    // List<Element> lis = xp.evaluate(doc);
    // XPathExpression<Element> xp1 = xpfac.compile("./a", Filters.element());
    // for (Element li : lis) {
    // Element anchor = xp1.evaluateFirst(li);
    // String teamName = anchor.getText();
    // String href = anchor.getAttributeValue("href");
    // // E.g. "/en/team/INS/team-ineos"
    // String[] parts = href.substring(1).split("/");
    // String abbrev = Text.normalizeString(parts[2]);
    // String teamCountry = Text.normalizeString(parts[0]);
    //
    // Team team = new Team(teamName, teamCountry);
    // teams.add(team);
    // team.setAbbrev(abbrev);
    //
    // Element span;
    // XPathExpression<Element> xp3 = xpfac
    // .compile("./table", Filters.element());
    // Element table = xp3.evaluateFirst(li);
    // XPathExpression<Element> xp4 = xpfac.compile("./tr", Filters.element());
    // List<Element> trs = xp4.evaluate(table);
    // for (Element tr : trs) {
    // XPathExpression<Element> xp5 = xpfac.compile("./td[@class=\"bib\"]",
    // Filters.element());
    // Element td = xp5.evaluateFirst(tr);
    // int riderNumber = Integer.parseInt(td.getTextNormalize());
    // XPathExpression<Element> xp6 = xpfac.compile("./td[@class=\"rider\"]",
    // Filters.element());
    // td = xp6.evaluateFirst(tr);
    // span = td.getChild("span");
    // Attribute classAttr = span.getAttribute("class");
    // String riderCountry = null;
    // for (String ss : classAttr.getValue().split("\\s+")) {
    // if (ss.startsWith("flag-")) {
    // riderCountry = ss.substring("flag-".length());
    // break;
    // }
    // }
    // anchor = td.getChild("a");
    // String riderName = anchor.getTextNormalize();
    // String[] firstAndLastName = getFirstAndLastName(riderName);
    // String firstName = firstAndLastName[0];
    // String lastName = firstAndLastName[1];
    // Cyclist cyclist = new Cyclist(riderNumber, firstName, lastName,
    // riderCountry);
    // team.addCyclist(cyclist);
    // }
    // }
    return teams;
  }

  private static Element evalFirstE(Object context, String xpath) {
    XPathExpression<Element> xpe = xpeCache.get(xpath);
    if (xpe == null) {
      xpe = xpfac.compile(xpath, Filters.element());
      xpeCache.put(xpath, xpe);
    }
    Element e = xpe.evaluateFirst(context);
    return e;
  }
  private static List<Element> evalAllE(Object context, String xpath) {
    XPathExpression<Element> xpe = xpeCache.get(xpath);
    if (xpe == null) {
      xpe = xpfac.compile(xpath, Filters.element());
      xpeCache.put(xpath, xpe);
    }
    List<Element> es = xpe.evaluate(context);
    return es;
  }
  private static Attribute evalFirstA(Object context, String xpath) {
    XPathExpression<Attribute> xpa = xpaCache.get(xpath);
    if (xpa == null) {
      xpa = xpfac.compile(xpath, Filters.attribute());
      xpaCache.put(xpath, xpa);
    }
    Attribute a = xpa.evaluateFirst(context);
    return a;
  }
  private static void writeTeamsXml(List<Team> teams)
      throws JDOMException, IOException, TransformerException {
    Element root = new Element("teams");
    Document teamsDoc = new Document(root);
    for (Team team : teams) {
      Element teamNode = new Element("team");
      root.addContent(teamNode);
      Element teamNameNode = new Element("name");
      teamNode.addContent(teamNameNode);
      teamNameNode.setText(team.getName());
      Element teamCountryNode = new Element("country");
      teamNode.addContent(teamCountryNode);
      teamCountryNode.setText(team.getCountry());
      Element teamAbbrevNode = new Element("abbrev");
      teamNode.addContent(teamAbbrevNode);
      teamAbbrevNode.setText(team.getAbbrev());
      Element mgrsNode = new Element("managers");
      teamNode.addContent(mgrsNode);
      for (String mgr : team.getManagers()) {
        Element mgrNode = new Element("manager");
        mgrsNode.addContent(mgrNode);
        mgrNode.setText(mgr);
      }
      for (Cyclist c : team.getCyclists()) {
        Element cNode = new Element("cyclist");
        teamNode.addContent(cNode);
        Element nameNode = new Element("name");
        nameNode.setText(c.getName());
        cNode.addContent(nameNode);
        Element numNode = new Element("number");
        numNode.setText("" + c.getNumber());
        cNode.addContent(numNode);
        Element countryNode = new Element("country");
        countryNode.setText(c.getCountry());
        cNode.addContent(countryNode);
      }
    }
    FileOutputStream fos = new FileOutputStream(new File("data/teams.xml"));
    printDocument(teamsDoc, fos);
    fos.close();
  }
  private static String[] getFirstAndLastName(String name) {
    String words[] = name.split("\\s+");
    int numUppercaseWords = 0;
    for (int i = 0; i < words.length; ++i) {
      if (StringUtil.isUpperCase(words[i])) {
        ++numUppercaseWords;
      }
      else {
        break;
      }
    }
    if (numUppercaseWords == words.length) {
      // BARGUIL WARREN
      // Assume last word is the first name.
      numUppercaseWords = words.length - 1;
      int i = words.length - 1;
      char[] chars = words[i].toLowerCase().toCharArray();
      chars[0] = Character.toUpperCase(chars[0]);
      words[i] = new String(chars);
    }
    String lastName = "";
    for (int i = 0; i < numUppercaseWords; ++i) {
      if (i > 0) {
        lastName += " ";
      }
      lastName += words[i];
    }
    String firstName = "";
    for (int i = numUppercaseWords; i < words.length; ++i) {
      if (i > numUppercaseWords) {
        firstName += " ";
      }
      firstName += words[i];
    }
    return new String[] { firstName, lastName };
  }
  private static void printTeamJerseys(List<Team> teams)
      throws JDOMException, IOException, TransformerException {
    Element html = new Element("html");
    Document doc = new Document(html);
    Element head = new Element("head");
    html.addContent(head);
    Element linkCss = new Element("link");
    head.addContent(linkCss);
    linkCss.setAttribute("type", "text/css");
    linkCss.setAttribute("rel", "stylesheet");
    linkCss.setAttribute("href", ProcessStyleSheet.CssFileName1);
    linkCss = new Element("link");
    head.addContent(linkCss);
    linkCss.setAttribute("rel", "stylesheet");
    linkCss.setAttribute("href", handCraftedStylesFileName);

    Element style = new Element("style");
    head.addContent(style);
    style.setAttribute("type", "text/css");
    style.setText(
          "body {\n"
        + "  font-family: Arial, Helvetica, sans-serif;\n"
        + "}\n"
        + "div#teamsIndexLink {\n"
        + "  position: fixed;\n"
        + "  top: 0;\n"
        + "  right: 0;\n"
        + "  border: 3px solid #8AC007;\n"
        + "  font-size: 200%\n"
        + "  background: white;\n"
        + "}\n"
    );

    Element body = new Element("body");
    html.addContent(body);
    body.setAttribute("class", "clearfix");

    Element div = new Element("div");
    body.addContent(div);
    div.setAttribute("id", "teamsIndexLink");
    Element anchor = new Element("a");
    div.addContent(anchor);
    anchor.setText("teams");
    anchor.setAttribute("href", "teamsIndex.html");

    for (Team team : teams) {
      anchor = new Element("a");
      body.addContent(anchor);
      anchor.setAttribute("href",
          "teamsIndex.html#team_" + TeamNameId.getTeamNameId(team.getName()));
      Element span0 = new Element("span");
      anchor.addContent(span0);
      span0.setAttribute("class", "span-img jerseyTeam--" + team.getAbbrev());
      Element span1 = new Element("span");
      anchor.addContent(span1);
      span1.setAttribute("class", "span-name");
      span1.setText(team.getName());
    }
    FileOutputStream fos = new FileOutputStream("data/teamsJerseys.html");
    printDocument(doc, fos);
  }
  private static void printTeamsIndex(List<Team> teams)
      throws JDOMException, IOException, TransformerException {
    Element html = new Element("html");
    Document doc = new Document(html);
    Element head = new Element("head");
    html.addContent(head);
    Element style = new Element("style");
    style.setAttribute("type", "text/css");
    head.addContent(style);
    style.setText(
          "body {\n"
        + "  font-family: Arial, Helvetica, sans-serif;\n"
        + "}\n"
        + "td {\n"
        + "  padding: 0 30 0 0;\n"
        + "}\n"
        + ".team td:nth-child(1) { width: 80; }\n"
        + ".team td:nth-child(3) { width: 100; }\n"
        + "div#cyclistsIndexLink {\n"
        + "  position: fixed;\n"
        + "  top: 0;\n"
        + "  right: 0;\n"
        + "  border: 3px solid #8AC007;\n"
        + "  font-size: 200%\n"
        + "  background: white;\n"
        + "}\n"
        + "div#teamNamesLink {\n"
        + "  position: fixed;\n"
        + "  top: 50;\n"
        + "  right: 0;\n"
        + "  border: 3px solid #8AC007;\n"
        + "  font-size: 200%\n"
        + "  background: white;\n"
        + "}\n"
    );
    Element script = new Element("script");
    head.addContent(script);
    script.setText("\n"
       + "function myLoad() {\n"
       + "   let myString = window.location.href;\n"
       + "   let myRegexp = /(?:[^#]*)#(.*)/;\n"
       + "   let match = myRegexp.exec(myString);\n"
       + "   let id = decodeURIComponent(match[1]);\n"
       + "   console.log(\"id: \" + id);\n"
       + "   let elem = document.getElementById(id);\n"
       + "   elem.style.backgroundColor = '#e5e5e5';\n"
       + " }\n"
        );
    Element body = new Element("body");
    body.setAttribute("onload", "myLoad()");
    html.addContent(body);
    Element div = new Element("div");
    body.addContent(div);
    div.setAttribute("id", "cyclistsIndexLink");
    Element anchor = new Element("a");
    div.addContent(anchor);
    anchor.setText("cyclists index");
    anchor.setAttribute("href", "cyclistsIndex.html");
    div = new Element("div");
    body.addContent(div);
    div.setAttribute("id", "teamNamesLink");
    anchor = new Element("a");
    div.addContent(anchor);
    anchor.setText("team names");
    anchor.setAttribute("href", "teamNamesIndex.html");
    Element table = new Element("table");
    body.addContent(table);
    int row = 0;
    int col = 0;
    Element tr = null;
    for (Team team : teams) {
      if (col == 0) {
        tr = new Element("tr");
        table.addContent(tr);
      }
      Element td = new Element("td");
      tr.addContent(td);
      td.addContent(createTeamTable(team));
      ++col;
      if (col == 2) {
        ++row;
        col = 0;
      }
    }
    FileOutputStream fos = new FileOutputStream("data/teamsIndex.html");
    printDocument(doc, fos);
  }
  private static Element createTeamTable(Team team) {
    Element table = new Element("table");
    table.setAttribute("class", "team");
    table.setAttribute("id",
        "team_" + TeamNameId.getTeamNameId(team.getName()));
    //
    Element thead = new Element("thead");
    table.addContent(thead);
    thead.setAttribute("align", "left");
    Element tr = new Element("tr");
    thead.addContent(tr);
    Element th = new Element("th");
    tr.addContent(th);
    th.setText(team.getName());
    th.setAttribute("colspan", "3");
    //
    List<Cyclist> cyclists = team.getCyclists();
    for (Cyclist cyclist : cyclists) {
      tr = new Element("tr");
      table.addContent(tr);
      Element td = new Element("td");
      tr.addContent(td);
      td.setText("" + cyclist.getNumber());
      td = new Element("td");
      tr.addContent(td);
      td.setText(cyclist.getName());
      td = new Element("td");
      tr.addContent(td);
      td.setText(cyclist.getCountry());
    }
    return table;
  }
  private static void printTeamNamesIndex(List<Team> teams)
      throws JDOMException, IOException, TransformerException {
    TreeMap<String, Integer> teamsIndex = new TreeMap<String, Integer>();
    for (Team team : teams) {
      String name = team.getName();
      if (name.toLowerCase().startsWith("team ")) {
        name = name.substring("team ".length());
      }
      teamsIndex.put(name, team.getCyclists().get(0).getNumber() / 10);
    }
    Element html = new Element("html");
    Document doc = new Document(html);
    Element style = new Element("style");
    style.setAttribute("type", "text/css");
    html.addContent(style);
    style.setText(
         "td {\n"
       + "  padding: 0 30 0 0;\n"
       + "}\n"
    );
    Element body = new Element("body");
    html.addContent(body);
    Element table = new Element("table");
    body.addContent(table);
    for (String teamName : teamsIndex.keySet()) {
      Element tr = new Element("tr");
      table.addContent(tr);
      Element td = new Element("td");
      tr.addContent(td);
      Element anchor = new Element("a");
      anchor.setAttribute("href",
        "teamsIndex.html#team_" + TeamNameId.getTeamNameId(teamName));
      anchor.setText(teamName);
      td.addContent(anchor);
      td = new Element("td");
      tr.addContent(td);
      td.setText("" + teamsIndex.get(teamName));
    }
    FileOutputStream fos = new FileOutputStream("data/teamNamesIndex.html");
    printDocument(doc, fos);
  }
  private static void printTeamsJson(List<Team> teams)
      throws JDOMException, IOException {
    FileWriter fw = new FileWriter("data/teams.json", StandardCharsets.UTF_8);
    fw.write("[" + nl);
    for (int i = 0; i < teams.size(); ++i) {
      Team team = teams.get(i);
      fw.write("{" + nl);
      fw.write("  \"name\": \"" + team.getName() + "\"," + nl);
      fw.write("  \"country\": \"" + team.getCountry() + "\"," + nl);
      fw.write("  \"abbrev\": \"" + team.getAbbrev() + "\"," + nl);
      fw.write("  \"cyclists\": [" + nl);
      for (int j = 0; j < team.getCyclists().size(); ++j) {
        Cyclist cyclist = team.getCyclists().get(j);
        fw.write("  {" + nl);
        fw.write("    \"number\": \"" + cyclist.getNumber() + "\"," + nl);
        fw.write("    \"name\": \"" + cyclist.getName() + "\"," + nl);
        fw.write("    \"country\": \"" + cyclist.getCountry() + "\"" + nl);
        fw.write("  }" + (j < (team.getCyclists().size()-1) ? "," : "") + nl);
      }
      fw.write("  ]" + nl);
      fw.write("}" + (i < teams.size() - 1 ? "," : "") + nl);
    }
    fw.write("]\n");
    fw.close();
  }

  private static String foreign = "ÀÁÂÃÄÅÆÇÈÉÊËÌÍÎÏÐÑÒÓÔÕÖ×ØÙÚÛÜÝÞßàáâãäåæçèéêëìíîïðñòóôõö÷øùúûüýþÿ";
  private static String european = "AAAAAAACEEEEIIIIDNOOOOOxOUUUUY_baaaaaaaceeeeiiiidnooooo÷øuuuuyþy";

  public static String toEuropean(String s) {
    StringBuilder sb = new StringBuilder(s.length());
    for (int i = 0; i < s.length(); ++i) {
      char c = s.charAt(i);
      int ix = foreign.indexOf(c);
      sb.append(ix == -1 ? c : european.charAt(ix));
    }
    return sb.toString();
  }
  public static void printCyclistsIndex(List<Team> teams)
      throws JDOMException, IOException, TransformerException {
    TreeMap<String, ArrayList<Object>> cyclistsIndex = new TreeMap<String, ArrayList<Object>>();
    for (Team team : teams) {
      for (Cyclist c : team.getCyclists()) {
        String name = c.getName();
        String[] parts = name.split("\\s+");
        for (String part : parts) {
          ArrayList<Object> value = new ArrayList<Object>();
          value.add(part);
          value.add(c);
          value.add(team);
          String key = toEuropean(part);
          cyclistsIndex.put(key.toLowerCase() + " " + name, value);
        }
      }
    }
    Element html = new Element("html");
    Document doc = new Document(html);
    Element style = new Element("style");
    style.setAttribute("type", "text/css");
    html.addContent(style);
    style.setText(
          "body {\n"
        + "  font-family: Arial, Helvetica, sans-serif;\n"
        + "}\n"
        + "td {\n"
        + "  padding: 0 10 0 0;\n"
        + "}\n"
        + ".cycnum {\n"
        + "  text-align: right;\n"
        + "  padding-right: 30;\n"
        + "}\n"
        + "div#topbar {\n"
        + "  position: fixed;\n"
        + "  top: 0;\n"
        + "  left: 0;\n"
        + "  border: 3px solid #8AC007;\n"
        + "  font-size: 200%;\n"
        + "  background: white;\n"
        + "}\n"
        + "table tr:nth-child(even) {background: #EEE};\n"
        + "table tr:nth-child(odd) {background: #FFF};\n"
    );
    Element body = new Element("body");
    html.addContent(body);
    Element div = new Element("div");
    div.setAttribute("id", "topbar");
    body.addContent(div);
    int a = (int) 'a';
    int z = (int) 'z';
    for (int i = a; i < z; ++i) {
      Element anchor = new Element("a");
      char upper = Character.toUpperCase((char) i);
      anchor.setAttribute("href", "#" + upper + "_anchor");
      anchor.setText("" + upper);
      div.addContent(anchor);
    }
    //
    div = new Element("div");
    div.setText(" ");
    div.setAttribute("style", "height: 30px;");
    body.addContent(div);
    //
    Element table = new Element("table");
    body.addContent(table);
    {
      Element thead = new Element("thead");
      table.addContent(thead);
      thead.setAttribute("align", "left");
      Element tr = new Element("tr");
      thead.addContent(tr);
      for (String s : new String[] { "Key", "Name", "Number", "Country",
          "Team" }) {
        Element th = new Element("th");
        tr.addContent(th);
        th.setText(s);
      }
    }
    Element tbody = new Element("tbody");
    table.addContent(tbody);
    Character firstChar = null;
    for (ArrayList<Object> value : cyclistsIndex.values()) {
      String part = (String) value.get(0);
      Cyclist c = (Cyclist) value.get(1);
      Team t = (Team) value.get(2);
      Element tr = new Element("tr");
      tbody.addContent(tr);
      Element td = new Element("td");
      tr.addContent(td);
      Character fc = part.charAt(0);
      boolean sameFirstChar = firstChar != null && firstChar.equals(fc);
      if (!sameFirstChar) {
        firstChar = fc;
        tr.setAttribute("id", firstChar + "_anchor");
      }
      td.setText(part);
      td = new Element("td");
      tr.addContent(td);
      td.setText(c.getName());
      td = new Element("td");
      tr.addContent(td);
      td.setAttribute("class", "cycnum");
      td.setText("" + c.getNumber());
      td = new Element("td");
      tr.addContent(td);
      td.setText(c.getCountry());
      td = new Element("td");
      tr.addContent(td);
      // td.setText(t.getName());
      Element anchor = new Element("a");
      anchor.setAttribute("href",
          "teamsIndex.html#team_" + TeamNameId.getTeamNameId(t.getName()));
      anchor.setText(t.getName());
      td.addContent(anchor);
    }
    FileOutputStream fos = new FileOutputStream("data/cyclistsIndex.html");
    printDocument(doc, fos);

  }
  public static Document getDoc(String fileName) throws IOException {
    return getDoc(new File(fileName));
  }
  public static org.jdom2.Document getDoc(File file) throws IOException {
    InputStream is = new FileInputStream(file);
    org.w3c.dom.Document doc = getTidy().parseDOM(is, null);
    // try {
    // printDocument(doc, System.out);
    // }
    // catch (TransformerException e) {
    // e.printStackTrace();
    // }
    return new DOMBuilder().build(doc);
  }
  public static void writeInputStreamToOutputStream(InputStream is,
      OutputStream os) throws IOException {
    while (true) {
      int i = is.read();
      if (i == -1) {
        break;
      }
      os.write(i);
    }
  }
  // public static String convertStreamToString(InputStream is) {
  // java.util.Scanner s = new java.util.Scanner(is);
  // s.useDelimiter("\\A");
  // String rv = s.hasNext() ? s.next() : "";
  // s.close();
  // return rv;
  // }
}
