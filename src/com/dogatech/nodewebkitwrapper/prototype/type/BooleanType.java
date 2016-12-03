package com.dogatech.nodewebkitwrapper.prototype.type;

import com.dogatech.nodewebkitwrapper.grammar.nodewebkitwrapperParser;
import com.dogatech.nodewebkitwrapper.io.Outputter;


public class BooleanType extends CppType {

  @Override
  public boolean isType(String name) {
    return name.equals("bool");
  }

  @Override
  public String v8Type() {
    return "v8::Boolean";
  }

  @Override
  public void outputUnwrap(String from, String to) {
    o.i().p("bool " + to + "(" + from + "->BooleanValue());");
  }
}