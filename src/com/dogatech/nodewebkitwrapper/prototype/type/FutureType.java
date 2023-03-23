package com.dogatech.nodewebkitwrapper.prototype.type;

import com.dogatech.nodewebkitwrapper.grammar.nodewebkitwrapperParser;
import com.dogatech.nodewebkitwrapper.io.Outputter;
import com.dogatech.nodewebkitwrapper.prototype.CppMethod;


public class FutureType extends CppType {

  @Override
  public boolean isType(String name) {
    return name.startsWith("future") || name.startsWith("std::future");
  }

  @Override
  public void outputResult() {
  }

  @Override
  public void outputReturn() {
  }
}