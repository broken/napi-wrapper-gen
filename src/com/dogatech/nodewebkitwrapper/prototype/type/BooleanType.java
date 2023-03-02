package com.dogatech.nodewebkitwrapper.prototype.type;

import com.dogatech.nodewebkitwrapper.grammar.nodewebkitwrapperParser;
import com.dogatech.nodewebkitwrapper.io.Outputter;


public class BooleanType extends CppType {

  @Override
  public boolean isType(String name) {
    return name.equals("bool");
  }

  @Override
  public void outputWrap(String var) {
    o.p("Napi::Boolean::New(info.Env(), " + var + ")", false);
  }

  @Override
  public void outputUnwrap(String from, String to) {
    o.i().p("bool " + to + "(" + from + ".As<Napi::Boolean>().Value());");
  }
}