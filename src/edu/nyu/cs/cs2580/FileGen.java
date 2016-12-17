/**
 * Created by yonghong on 11/28/16.
 */
package edu.nyu.cs.cs2580;

import java.io.*;

/**
 * This is helper classs to generate output files
 */
public class FileGen {
  public static void generate(String results, String folderName, String fileName) {
    PrintWriter pw = null;
    try {
      checkFile(folderName, fileName);
      pw = new PrintWriter(new FileWriter(folderName + "/" + fileName, false));
    } catch (IOException e) {
      e.printStackTrace();
    }
    pw.append(results);
    pw.close();
  }

  private static void checkFile(String folderName, String fileName) throws IOException {

    File folder = new File("./" + folderName);
    if (!folder.exists())
      folder.mkdir();

    File tsvFile = new File("./" + folderName + "/" + fileName);
    if (!tsvFile.exists())
      tsvFile.createNewFile();
  }

  public static void appendDataToFile(String results, String folderName, String fileName) {
    PrintWriter pw = null;
    try {
      checkFile(folderName, fileName);
      pw = new PrintWriter(new FileWriter(folderName + "/" + fileName, true));
    } catch (IOException e) {
      e.printStackTrace();
    }
    pw.append(results);
    pw.close();
  }
}