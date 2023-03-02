package com.dogatech.nodewebkitwrapper.prototype.type;

import com.dogatech.nodewebkitwrapper.grammar.nodewebkitwrapperParser;
import com.dogatech.nodewebkitwrapper.io.Outputter;


public class NumberType extends CppType {

  @Override
  public boolean isType(String name) {
    return name.equals("float") || name.equals("double");
  }

  @Override
  public void outputWrap(String var) {
    o.p("Napi::Number::New(info.Env(), " + var + ")", false);
  }

  @Override
  public void outputUnwrap(String from, String to) {
    o.i().p("double " + to + "(" + from + ".As<Napi::Number>().DoubleValue());");
  }
}