package com.dogatech.napiwrapper.io;


public abstract class Outputter {

  String indentSpaces = "";
  String tab = "  ";

  public abstract Outputter p(String s, boolean nl);

  public Outputter p(String s) {
    p(s, true);
    return this;
  }

  public Outputter i() {
    p(indentSpaces, false);
    return this;
  }

  public Outputter incIndent() {
    indentSpaces += tab;
    return this;
  }

  public Outputter decIndent() {
    if (indentSpaces.length() > tab.length()) {
      indentSpaces = indentSpaces.substring(0, indentSpaces.length() - tab.length());
    } else {
      indentSpaces = "";
    }
    return this;
  }
}