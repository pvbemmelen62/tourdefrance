package nl.xs4all.pvbemmel.letour;

import java.awt.image.*;
import java.io.*;

import javax.imageio.*;
import javax.xml.transform.*;

import org.jdom2.*;

import static nl.xs4all.pvbemmel.letour.DocumentStuff.*;

public class JerseySeparator {
  private static final String inFileName = "data/sprite_jerseys.png";
  private static final int numJerseys = 22;
  private static final Object[] teamNumbers = {
    "18", Integer.valueOf(0), "SKY",
    "14", Integer.valueOf(1), "MOVISTAR TEAM",
    "21", Integer.valueOf(2), "KATUSHA",
    "19", Integer.valueOf(3), "TINKOFF-SAXO",
    "02", Integer.valueOf(4), "ASTANA PRO TEAM",
    "12", Integer.valueOf(5), "CANNONDALE",
    "10", Integer.valueOf(6), "BELKIN PRO CYCLING",
    "15", Integer.valueOf(7), "OMEGA PHARMA-QUICK STEP",
    "00", Integer.valueOf(8), "AG2R LA MONDIALE",
    "08", Integer.valueOf(9), "GARMIN - SHARP",
    "01", Integer.valueOf(10), "GIANT-SHIMANO",
    "11", Integer.valueOf(11), "LAMPRE - MERIDA",
    "07", Integer.valueOf(12), "FDJ.FR",
    "13", Integer.valueOf(13), "LOTTO-BELISOL",
    "03", Integer.valueOf(14), "BMC RACING TEAM",
    "05", Integer.valueOf(15), "EUROPCAR",
    "20", Integer.valueOf(16), "TREK FACTORY RACING",
    "04", Integer.valueOf(17), "COFIDIS, SOLUTIONS CREDITS",
    "09", Integer.valueOf(18), "ORICA GREENEDGE",
    "16", Integer.valueOf(19), "IAM CYCLING",
    "17", Integer.valueOf(20), "NETAPP-ENDURA",
    "06", Integer.valueOf(21), "BRETAGNE - SECHE ENVIRONNEMENT",
  };
  

  private BufferedImage bimSprite;

  public static void main(String[] args) throws JDOMException, IOException,
      TransformerException {
    JerseySeparator js = new JerseySeparator();
    js.readJerseys();
    js.splitJerseys();
    js.printHtml();
  }
  private void readJerseys() {
    File file = new File(inFileName);
    bimSprite = null;
    try {
      bimSprite = ImageIO.read(file);
    }
    catch (IOException e) {
    }
  }
  private void splitJerseys() {
    int hs = bimSprite.getHeight();
    int h = hs / numJerseys;
    int x = 0;
    int y = 0;
    int w = bimSprite.getWidth();

    for (int i = 0; i < numJerseys; ++i) {
      BufferedImage bim = bimSprite.getSubimage(x, y, w, h);
      try {
        String snum = String.format("%02d", i);
        File outFileName = new File("jersey" + snum + ".png");
        ImageIO.write(bim, "png", outFileName);
        y += h;
      }
      catch (IOException e) {
        throw new IllegalStateException(e);
      }
    }
  }
  private void printHtml() throws JDOMException,
      IOException, TransformerException {
    
    Element html = new Element("html");
    Document doc = new Document(html);
    Element style = new Element("style");
    style.setAttribute("type", "text/css");
    html.addContent(style);
    style.setText(
        ".team {\n"
      + "  width: 10%;\n"
      + "  padding-bottom: 30px;\n"
      + "}\n"
      + ".teamName {\n"
      + "  text-align: center;\n"
      + "  height: 80px;\n"
      + "}\n"
      + ".teamNumber {\n"
      + "  text-align: center;\n"
      + "}\n"
    );
    Element body = new Element("body");
    html.addContent(body);
    Element table = new Element("table");
    body.addContent(table);
    Element tr = null;
    for(int i=0; i<22; ++i) {
      if((i%10)==0) {
        tr = new Element("tr");
        table.addContent(tr);
      }
      Element td = new Element("td");
      tr.addContent(td);
      td.setAttribute("class", "team");
      createSubTable(td, i);
    }
    FileOutputStream fos = new FileOutputStream("data/teamsJerseys.html");
    printDocument(doc, fos);
  }
  private void createSubTable(Element parent, int i) {
    Element table = new Element("table");
    parent.addContent(table);
    Element tr = new Element("tr");
    table.addContent(tr);
    Element td = new Element("td");
    tr.addContent(td);
    Element img = new Element("img");
    td.addContent(img);
    String fileName = "jersey" + teamNumbers[i * 3] + ".png";
    img.setAttribute("src", fileName);
    tr = new Element("tr");
    table.addContent(tr);
    td = new Element("td");
    td.setAttribute("class", "teamNumber");
    tr.addContent(td);
    td.setText(""+i);
    tr = new Element("tr");
    table.addContent(tr);
    td = new Element("td");
    tr.addContent(td);
    td.setAttribute("class", "teamName");
    td.setText(""+teamNumbers[i*3 + 2]);
  }
}
