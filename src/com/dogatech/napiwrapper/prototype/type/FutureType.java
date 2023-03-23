package com.dogatech.napiwrapper.prototype.type;

import com.dogatech.napiwrapper.grammar.napiwrapperParser;
import com.dogatech.napiwrapper.io.Outputter;
import com.dogatech.napiwrapper.prototype.CppMethod;


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