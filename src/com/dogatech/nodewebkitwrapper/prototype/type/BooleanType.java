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
    o.p("Napi::Boolean::New(env, " + var + ")", false);
  }

  @Override
  public void outputUnwrap(String from, String to) {
    o.i().p("if (!" + from + ".IsBoolean()) {").incIndent();
    o.i().p("Napi::TypeError::New(env, \"TypeError: Boolean expected (for " + from + ")\").ThrowAsJavaScriptException();");
    o.i().p("return", false);
    if (!isInVoidMethod) o.p(" env.Null()", false);
    o.p(";");
    o.decIndent().i().p("}");
    o.i().p("bool " + to + "(" + from + ".As<Napi::Boolean>().Value());");
  }
}