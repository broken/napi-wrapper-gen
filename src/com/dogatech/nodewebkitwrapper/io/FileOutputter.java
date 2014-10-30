package com.dogatech.nodewebkitwrapper.io;

import java.io.IOException;
import java.io.OutputStream;


public class FileOutputter extends Outputter {

  OutputStream os;

  public FileOutputter(OutputStream out) {
    this.os = out;
  }

  @Override
  public Outputter p(String s, boolean nl) {
    try {
      os.write(s.getBytes());
      if (nl) os.write("\n".getBytes());
    } catch (IOException e) {
      e.printStackTrace();
    }
    return this;
  }
}