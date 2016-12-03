package com.dogatech.nodewebkitwrapper.prototype.type;

import com.dogatech.nodewebkitwrapper.grammar.nodewebkitwrapperParser;
import com.dogatech.nodewebkitwrapper.io.Outputter;


public class TimeTType extends CppType {

  @Override
  public boolean isType(String name) {
    return name.equals("time_t");
  }

  @Override
  public String v8Type() {
    return "v8::Number";
  }

  @Override
  public void outputWrap(String var) {
    o.p("Nan::New<v8::Number>(" + var + " * 1000)", false);
  }

  @Override
  public void outputUnwrap(String from, String to) {
    o.i().p("time_t " + to + "(" + from + "->NumberValue() / 1000);");
  }
}