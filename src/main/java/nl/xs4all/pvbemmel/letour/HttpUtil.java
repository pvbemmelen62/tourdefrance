package nl.xs4all.pvbemmel.letour;

import java.io.*;

import org.apache.http.*;
import org.apache.http.client.*;
import org.apache.http.client.methods.*;
import org.apache.http.impl.client.*;

public class HttpUtil {

  public static String getURIContent(String uri) throws ClientProtocolException,
      IOException {
    HttpClientBuilder builder = HttpClientBuilder.create();
    CloseableHttpClient httpclient = builder.build();
    
//    String uri = "http://www.directtextbook.com/xml.php?"
//      + "key=609018ecad29c727beac9d9a809d0048&ean=" + isbn;
    HttpGet httpGet = new HttpGet(uri);
    HttpResponse response = httpclient.execute(httpGet);
    HttpEntity entity = response.getEntity();
    String content = StreamUtil.getAsString(entity.getContent());
    return content;
  }
}
