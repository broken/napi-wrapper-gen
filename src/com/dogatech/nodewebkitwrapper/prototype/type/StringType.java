package com.dogatech.nodewebkitwrapper.prototype.type;

import com.dogatech.nodewebkitwrapper.grammar.nodewebkitwrapperParser;
import com.dogatech.nodewebkitwrapper.io.Outputter;


public class StringType extends CppType {

  @Override
  public boolean isType(String name) {
    return name.startsWith("string") || name.startsWith("std::string");
  }

  @Override
  public void outputWrap(String var) {
    o.p("Napi::String::New(info.Env(), " + var + ")", false);
  }

  @Override
  public void outputUnwrap(String from, String to) {
    o.i().p("if (!" + from + ".IsString()) {").incIndent();
    o.i().p("Napi::TypeError::New(info.Env(), \"TypeError: String expected (for " + from + ")\").ThrowAsJavaScriptException();");
    o.i().p("return", false);
    if (!isInVoidMethod) o.p(" info.Env().Null()", false);
    o.p(";");
    o.decIndent().i().p("}");
    o.i().p("std::string " + to + "(" + from + ".As<Napi::String>().Utf8Value());");
  }
}