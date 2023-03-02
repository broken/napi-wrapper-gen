package com.dogatech.nodewebkitwrapper.prototype.type;

import com.dogatech.nodewebkitwrapper.grammar.nodewebkitwrapperParser;
import com.dogatech.nodewebkitwrapper.io.Outputter;


public class VoidType extends CppType {

  @Override
  public boolean isType(String name) {
    return name.equals("void");
  }

  @Override
  public void outputResult() {
  }

  @Override
  public void outputReturn() {
  }
}