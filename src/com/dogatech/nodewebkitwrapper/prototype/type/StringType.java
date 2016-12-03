package com.dogatech.nodewebkitwrapper.prototype.type;

import com.dogatech.nodewebkitwrapper.grammar.nodewebkitwrapperParser;
import com.dogatech.nodewebkitwrapper.io.Outputter;


public class StringType extends CppType {

  @Override
  public boolean isType(String name) {
    return name.startsWith("string");
  }

  @Override
  public String v8Type() {
    return "v8::String";
  }

  @Override
  public void outputWrap(String var) {
    o.p("Nan::New<" + v8Type() + ">(" + var + ").ToLocalChecked()", false);
  }

  @Override
  public void outputUnwrap(String from, String to) {
    o.i().p("string " + to + "(" + "*v8::String::Utf8Value(" + from + "->ToString()));");
  }
}