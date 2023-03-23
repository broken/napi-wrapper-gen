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
    o.p("Napi::Number::New(env, " + var + " * 1000)", false);
  }

  @Override
  public void outputUnwrap(String from, String to) {
    o.i().p("if (!" + from + ".IsNumber()) {").incIndent();
    o.i().p("Napi::TypeError::New(env, \"TypeError: Number/time expected (for " + from + ")\").ThrowAsJavaScriptException();");
    o.i().p("return", false);
    if (!isInVoidMethod) o.p(" env.Null()", false);
    o.p(";");
    o.decIndent().i().p("}");
    o.i().p("time_t " + to + "(" + from + ".As<Napi::Number>().Int32Value() / 1000);");
  }
}