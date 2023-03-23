package com.dogatech.napiwrapper.prototype.type;

import com.dogatech.napiwrapper.grammar.napiwrapperParser;
import com.dogatech.napiwrapper.io.Outputter;
import com.dogatech.napiwrapper.prototype.CppMethod;


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