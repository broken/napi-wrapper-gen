package com.dogatech.napiwrapper.prototype.type;

import com.dogatech.napiwrapper.grammar.napiwrapperParser;
import com.dogatech.napiwrapper.io.Outputter;
import com.dogatech.napiwrapper.prototype.CppMethod;


public class FunctionType extends CppType {

  @Override
  public boolean isType(String name) {
    return name.startsWith("function") || name.startsWith("std::function");
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
  public void outputUnwrap(String from, String to, CppMethod.MethodType mt) {
    o.i().p("if (!" + from + ".IsFunction()) {").incIndent();
    mt.errOut("TypeError: Function expected (for " + from + ")");
    o.decIndent().i().p("}");
    if (generics.size() == 1 && generics.get(0).name.equals("float"))
      outputUnwrapAsync(from, to);
    else
      outputUnwrapLocal(from, to);
  }

  private void outputUnwrapAsync(String from, String to) {
    o.i().p("Napi::Function " + to + " = " + from + ".As<Napi::Function>();");
  }

  private void outputUnwrapLocal(String from, String to) {
    // First unwrap into a Nan::Callback
    String f = to + "Fn";
    o.i().p("Napi::Function " + f + " = " + from + ".As<Napi::Function>();");
    // Next create a normal callback function to pass to method
    o.i().p("auto " + to + " = [&env, &" + f + "](", false);
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
    // Finally call the v8 callback from inside our c callback
    o.i().p(f + ".Call(env.Global(), {", false);
    for (int i = 0; i < generics.size(); ++i) {
      if (i > 0) o.p(", ");
      CppType t = generics.get(i);
      t.outputWrap("p" + i);
    }
    o.p("});");
    o.decIndent().i().p("};");
  }
}