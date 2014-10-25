package com.dogatech.soulsifter.nodewebkitwrapper;

import java.util.Arrays;

import com.dogatech.nodewebkitwrapper.WrapperTool;


public class SoulSifterWrapperTool {
  private static String[] files = {
      "resources/Song.h"
  };

  public static void main(String[] args) throws Exception {
    String[] newArgs = Arrays.copyOf(args, args.length + files.length);
    System.arraycopy(files, 0, newArgs, args.length, files.length);
    WrapperTool.main(newArgs);
  }
}