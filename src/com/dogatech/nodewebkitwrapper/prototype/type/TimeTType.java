package com.dogatech.nodewebkitwrapper.prototype.type;

import com.dogatech.nodewebkitwrapper.grammar.nodewebkitwrapperParser;
import com.dogatech.nodewebkitwrapper.io.Outputter;


public class TimeTType extends CppType {

  @Override
  public boolean isType(String name) {
    return name.equals("time_t");
  }

  @Override
  public void outputWrap(String var) {
    o.p("Napi::Number::New(info.Env(), " + var + " * 1000)", false);
  }

  @Override
  public void outputUnwrap(String from, String to) {
    o.i().p("time_t " + to + "(" + from + ".As<Napi::Number>().Int32Value() / 1000);");
  }
}