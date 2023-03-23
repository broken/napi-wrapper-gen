package com.dogatech.nodewebkitwrapper.prototype.type;

import com.dogatech.nodewebkitwrapper.grammar.nodewebkitwrapperParser;
import com.dogatech.nodewebkitwrapper.io.Outputter;
import com.dogatech.nodewebkitwrapper.prototype.CppMethod;


public class BooleanType extends CppType {

  @Override
  public boolean isType(String name) {
    return name.equals("bool");
  }

  @Override
  public void outputWrap(String var) {
    o.p("Napi::Boolean::New(env, " + var + ")", false);
  }

  @Override
  public void outputUnwrap(String from, String to, CppMethod.MethodType mt) {
    o.i().p("if (!" + from + ".IsBoolean()) {").incIndent();
    mt.errOut("TypeError: Boolean expected (for " + from + ")");
    o.decIndent().i().p("}");
    o.i().p("bool " + to + "(" + from + ".As<Napi::Boolean>().Value());");
  }
}