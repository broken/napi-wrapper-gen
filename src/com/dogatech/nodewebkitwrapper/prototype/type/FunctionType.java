package com.dogatech.nodewebkitwrapper.prototype.type;

import com.dogatech.nodewebkitwrapper.grammar.nodewebkitwrapperParser;
import com.dogatech.nodewebkitwrapper.io.Outputter;


public class FunctionType extends CppType {

  @Override
  public boolean isType(String name) {
    return name.startsWith("function") || name.startsWith("std::function");
  }

  @Override
  public String v8Type() {
    return "v8::Function";
  }

  @Override
  public void outputResult() {
    throw new RuntimeException("Not implemented.");
  }

  @Override
  public void outputReturn() {
    throw new RuntimeException("Not implemented.");
  }

  @Override
  public void outputUnwrap(String from, String to) {
    if (generics.size() == 1 && generics.get(0).name.equals("float"))
      outputUnwrapAsync(from, to);
    else
      outputUnwrapLocal(from, to);
  }

  private void outputUnwrapAsync(String from, String to) {
    o.i().p("Nan::Callback* " + to + " = new Nan::Callback();");
    o.i().p(to + "->Reset(" + from + ".As<v8::Function>());");
  }

  private void outputUnwrapLocal(String from, String to) {
    // First unwrap into a Nan::Callback
    String f = to + "Fn";
    o.i().p("Nan::Callback " + f + ";");
    o.i().p(f + ".Reset(" + from + ".As<v8::Function>());");
    // Next create a normal callback function to pass to method
    o.i().p("auto " + to + " = [&" + f + "](", false);
    for (int i = 0; i < generics.size(); ++i) {
      CppType t = generics.get(i);
      if (i > 0) o.p(", ", false);
      o.p(t.fullName(), false);
      for (String m : t.modifiers) {
        o.p(m, false);
      }
      o.p(" p" + i, false);
    }
    o.p(") {");
    o.incIndent();
    // Wrap values & add to array of args
    for (int i = 0; i < generics.size(); ++i) {
      CppType t = generics.get(i);
      o.i().p("v8::Local<v8::Value> v" + i + " = ", false);
      t.outputWrap("p" + i);
      o.p(";");
    }
    o.i().p("v8::Local<v8::Value> argv[] = {", false);
    for (int i = 0; i < generics.size(); ++i) {
      if (i > 0) o.p(", ", false);
      o.p("v" + i, false);
    }
    o.p("};");
    // Finally call the v8 callback from inside our c callback
    o.i().p(f + ".Call(" + generics.size() + ", " + "argv);");
    o.decIndent().i().p("};");
  }
}