package com.dogatech.nodewebkitwrapper.prototype.type;

import com.dogatech.nodewebkitwrapper.grammar.nodewebkitwrapperParser;
import com.dogatech.nodewebkitwrapper.io.Outputter;


public class IntegerType extends CppType {

  @Override
  public boolean isType(String name) {
    return name.equals("int");
  }

  @Override
  public String v8Type() {
    return "v8::Integer";
  }

  @Override
  public void outputUnwrap(String from, String to) {
    o.i().p("int " + to + "(" + from + "->IntegerValue());");
  }
}