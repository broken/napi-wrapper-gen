package com.dogatech.napiwrapper.prototype.type;

import com.dogatech.napiwrapper.grammar.napiwrapperParser;
import com.dogatech.napiwrapper.io.Outputter;


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