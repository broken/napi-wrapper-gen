package com.dogatech.nodewebkitwrapper.prototype.type;

import com.dogatech.nodewebkitwrapper.grammar.nodewebkitwrapperParser;
import com.dogatech.nodewebkitwrapper.io.Outputter;
import com.dogatech.nodewebkitwrapper.prototype.type.CppType;
import com.dogatech.nodewebkitwrapper.prototype.CppMethod;


public class TimeTType extends CppType {

  @Override
  public boolean isType(String name) {
    return name.equals("time_t");
  }

  @Override
  public void outputWrap(String var) {
    o.p("Napi::Number::New(env, " + var + " * 1000)", false);
  }

  @Override
  public void outputUnwrap(String from, String to, CppMethod.MethodType mt) {
    o.i().p("if (!" + from + ".IsNumber()) {").incIndent();
    mt.errOut("TypeError: Number/time expected (for " + from + ")");
    o.decIndent().i().p("}");
    o.i().p("time_t " + to + "(" + from + ".As<Napi::Number>().Int32Value() / 1000);");
  }
}