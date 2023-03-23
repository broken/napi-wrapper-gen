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
    o.p("Napi::Number::New(env, " + var + ")", false);
  }

  @Override
  public void outputUnwrap(String from, String to) {
    o.i().p("if (!" + from + ".IsNumber()) {").incIndent();
    o.i().p("Napi::TypeError::New(env, \"TypeError: Number expected (for " + from + ")\").ThrowAsJavaScriptException();");
    o.i().p("return", false);
    if (!isInVoidMethod) o.p(" env.Null()", false);
    o.p(";");
    o.decIndent().i().p("}");
    o.i().p("double " + to + "(" + from + ".As<Napi::Number>().DoubleValue());");
  }
}