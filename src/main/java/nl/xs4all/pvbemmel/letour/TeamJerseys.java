package nl.xs4all.pvbemmel.letour;

import static nl.xs4all.pvbemmel.letour.DocumentStuff.printDocument;
import static nl.xs4all.pvbemmel.letour.DocumentStuff.readDocument;

import java.io.*;
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

public class TeamJerseys {
  private static final String nl = System.getProperty("line.separator");
  private static Tidy tidy;
  private static final String cssURL =
      "http://www.letour.fr/css/v7/styles.20170719.css";
  private static String cssFileName;
//  private HashMap<String,String> abbrevFromTeamId

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

  /**
   * Parse teams page from www.letour.fr, and construct cyclistsIndex.html and
   * teamsIndex.html .
   * 
   * <pre>
   * In Firefox: File->Open Location->http://www.letour.fr
   * Click "TEAMS"
   * File->Save As->Web Page, HTML only,
   *     choose filename data/teams-jerseys-html-only.html
   * $ vi data/teams-jerseys-html-only.html
   * Change &lt;html&gt; shit in line 2 (1?) , to &lt;html&gt;
   * </pre>
   * @throws IOException 
   * @throws SAXException 
   * @throws ParserConfigurationException 
   * @throws TransformerException 
   * @throws JDOMException 
   */
  public static void main(String[] args) throws IOException, JDOMException,
       ParserConfigurationException, SAXException, TransformerException {
    {
      String[] ss = cssURL.split("/");
      cssFileName = ss[ss.length-1];
    }
    File file = new File("data/teams-jerseys-tidy.html");
    Document doc = null;
    if (file.exists()) {
      FileInputStream fis = new FileInputStream(file);
      doc = readDocument(fis);
      fis.close();
    }
    else {
      doc = getDoc("data/teams-jerseys-html-only.html");
      printDocument(doc, new FileOutputStream(file));
    }
    // To understand the html structure, in firefox use firebug to look at the
    // HTML Dom .
    
    XPathFactory xpfac = XPathFactory.instance();
    doStuff(xpfac, doc);
  }

  private static void doStuff0(XPathFactory xpfac, Document doc)
      throws FileNotFoundException, IOException, JDOMException,
       TransformerException {
    XPathExpression<Element> xp = xpfac.compile(
        "//ul[@class=\"clearfix subnav__teams\"]",
          Filters.element());
      List<Element> lst = xp.evaluate(doc);
      Element ul = lst.get(0);

//      Format format = null;
//      XMLOutputProcessor processor = null;
//      XMLOutputter xmlOut = new XMLOutputter(format, processor);
//      xmlOut.output(ul, new FileOutputStream("data/ul-output.xml"));
      
      Document docOut = new Document();
      Element html = new Element("html");
      docOut.setRootElement(html);
      Element head = new Element("head");
      html.addContent(head);
      Element link = new Element("link");
      link.setAttribute("type", "text/css");
      // Create pauls...css with nl.xs4all.pvbemmel.letour.css.StripStyleSheet.
      link.setAttribute("href", "pauls-styles.20170719.css");
      link.setAttribute("rel", "stylesheet");
      link.setAttribute("media", "all");
      head.addContent(link);
      Element body = new Element("body");
      html.addContent(body);
      body.addContent(ul.clone()); // will produce <span .... /> which,
      // when part of a html file with doctype xml declaration, is not
      // correctly parsed by firefox: two sibling spans are parsed as nested.
      DocumentStuff.printDocument(docOut, new FileOutputStream(
          "data/ul-output.html"));
  }
  private static void doStuff(XPathFactory xpfac, Document doc) {
    XPathExpression<Element> xp = xpfac.compile(
      "//ul[@class=\"clearfix subnav__teams\"]/li[@class=\"subnav__teams__item\"]",
        Filters.element());
    List<Element> lis = xp.evaluate(doc);
    

    System.out.println("subnav_teams before");
    
    XPathExpression<Element> xp1 = xpfac.compile(
        "./a/span", Filters.element());
    for (Element li : lis) {
      List<Element> spans = xp1.evaluate(li);
      Element span;
      span = spans.get(0);
      Attribute attrib = span.getAttribute("class");
      String[] ss = attrib.getValue().split("\\s");
      System.out.println("css class selector: " + ss[ss.length-1]);
      span = spans.get(1);
      String aText = span.getValue();
      String teamName = Text.normalizeString(aText);
      System.out.println("subnav__teams__name: " + teamName);
    }
    System.out.println("subnav_teams after");
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

}
