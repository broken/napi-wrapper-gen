package com.dogatech.nodewebkitwrapper.prototype.type;

import com.dogatech.nodewebkitwrapper.grammar.nodewebkitwrapperParser;
import com.dogatech.nodewebkitwrapper.io.Outputter;
import com.dogatech.nodewebkitwrapper.prototype.CppMethod;


public class NumberType extends CppType {

  @Override
  public boolean isType(String name) {
    return name.equals("float") || name.equals("double");
  }

  @Override
  public void outputWrap(String var) {
    o.p("Napi::Number::New(env, " + var + ")", false);
  }

  @Override
  public void outputUnwrap(String from, String to, CppMethod.MethodType mt) {
    o.i().p("if (!" + from + ".IsNumber()) {").incIndent();
    mt.errOut("TypeError: Number expected (for " + from + ")");
    o.decIndent().i().p("}");
    o.i().p("double " + to + "(" + from + ".As<Napi::Number>().DoubleValue());");
  }
}