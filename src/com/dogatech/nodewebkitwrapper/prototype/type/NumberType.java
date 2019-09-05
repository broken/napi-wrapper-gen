package com.dogatech.nodewebkitwrapper.prototype.type;

import com.dogatech.nodewebkitwrapper.grammar.nodewebkitwrapperParser;
import com.dogatech.nodewebkitwrapper.io.Outputter;


public class NumberType extends CppType {

  @Override
  public boolean isType(String name) {
    return name.equals("float") || name.equals("double");
  }

  @Override
  public String v8Type() {
    return "v8::Number";
  }

  @Override
  public void outputUnwrap(String from, String to) {
    o.i().p("double " + to + "(" + from + "->NumberValue());");
  }
}