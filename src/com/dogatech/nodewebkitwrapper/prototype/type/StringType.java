package com.dogatech.nodewebkitwrapper.prototype.type;

import com.dogatech.nodewebkitwrapper.grammar.nodewebkitwrapperParser;
import com.dogatech.nodewebkitwrapper.io.Outputter;
import com.dogatech.nodewebkitwrapper.prototype.CppMethod;


public class StringType extends CppType {

  @Override
  public boolean isType(String name) {
    return name.startsWith("string") || name.startsWith("std::string");
  }

  @Override
  public void outputWrap(String var) {
    o.p("Napi::String::New(env, " + var + ")", false);
  }

  @Override
  public void outputUnwrap(String from, String to, CppMethod.MethodType mt) {
    o.i().p("if (!" + from + ".IsString()) {").incIndent();
    mt.errOut("TypeError: String expected (for " + from + ")");
    o.decIndent().i().p("}");
    o.i().p("std::string " + to + "(" + from + ".As<Napi::String>().Utf8Value());");
  }
}