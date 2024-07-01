package nl.xs4all.pvbemmel.letour;

import java.awt.*;
import java.awt.image.*;
import java.io.*;

import javax.imageio.*;

import org.apache.http.client.methods.*;
import org.apache.http.impl.client.*;

public class CyclistImages {

  public static void main(String[] args) throws IOException {
    //downloadAllImages();
    mergeImages();
  }
  public static void downloadAllImages() throws IOException {
    for(int team=0; team<22; ++team) {
      for(int index=1; index<10; ++index) {
        int nummer = 10*team + index;
        downloadImage(nummer);
      }
    }
  }
  public static void downloadImage(int nummer) throws IOException {
    CloseableHttpClient httpclient = HttpClients.createDefault();
    HttpGet httpget = new HttpGet(
        "http://www.letour.fr/PHOTOS/TDF/2017/RIDERS/"+nummer+".jpg");
    CloseableHttpResponse response = httpclient.execute(httpget);
    BufferedOutputStream bos = null;
    try {
        InputStream is = response.getEntity().getContent();
        FileOutputStream fos = new FileOutputStream(
            "data/cyclistImages/"+nummer+".jpg");
        bos = new BufferedOutputStream(fos);
        int b;
        while((b = is.read()) != -1 ) {
          fos.write(b);
        }
    } finally {
        response.close();
        if(bos!=null) {
          bos.close();
        }
    }
  }
  public static void mergeImages() throws IOException {
    // https://stackoverflow.com/questions/3922276/how-to-combine-multiple-
    //   pngs-into-one-big-png-file
    
    int imgWidth = 135;
    int imgHeight = 135;
    int resultWidth = imgWidth*9;
    int resultHeight = imgHeight*22;
    BufferedImage result = new BufferedImage(
      resultWidth, resultHeight,
      BufferedImage.TYPE_INT_RGB);
    Graphics g = result.getGraphics();
    
    for(int team=0; team<22; ++team) {
      for(int index=1; index<10; ++index) {
        int cyclist = team*10+index;
        String fileNameIn = "data/cyclistImages/" + cyclist + ".jpg";
        BufferedImage bi = ImageIO.read(new File(fileNameIn));
        int x = (index-1)*imgWidth;
        int y = team*imgHeight;
        g.drawImage(bi, x, y, null);
      }
    }
    ImageIO.write(result,"png",new File("data/allCyclistImages.jpg"));
  }
  public static void writeCyclistImagesHtml() {
    
  }
}
