package nl.xs4all.pvbemmel.letour;

import java.io.*;
import java.nio.charset.*;
import java.nio.file.*;
import java.util.*;

public class FileUtil {

  public static String readFile(String path) throws IOException {
    //Charset charset = StandardCharsets.UTF_8;
    Charset charset = Charset.defaultCharset();
    return readFile(path, charset);
  }
  public static String readFile(String path, Charset encoding) 
      throws IOException {
    byte[] bytes = Files.readAllBytes(Paths.get(path));
    return new String(bytes, encoding);
  }
  public static List<String> readFileLines(String path) throws IOException {
    Charset charset = Charset.defaultCharset();
    return readFileLines(path, charset);
  }
  public static List<String> readFileLines(String path, Charset encoding)
      throws IOException {
    List<String> lines = Files.readAllLines(Paths.get(path), encoding);
    return lines;
  }
  public static List<String> readFile2(String fileName) throws IOException {
    ArrayList<String> lines = new ArrayList<>();
    try (BufferedReader br = new BufferedReader(new FileReader(fileName))) {
      String line = null;
      do {
        line = br.readLine();
//        if(line.trim().isEmpty()) {
//          continue;
//        }
        lines.add(line);
      }
      while(line != null);
    }
    return lines;
  }
  
}
