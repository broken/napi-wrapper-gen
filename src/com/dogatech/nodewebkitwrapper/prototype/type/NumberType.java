package com.dogatech.nodewebkitwrapper.prototype.type;

import com.dogatech.nodewebkitwrapper.grammar.nodewebkitwrapperParser;
import com.dogatech.nodewebkitwrapper.io.Outputter;


public class NumberType extends CppType {

  @Override
  public boolean isType(String name) {
    return name.equals("int");
  }

  @Override
  public void outputWrap(String var) {
    o.p("NanNew<v8::Number>(" + var + ")", false);
  }
}