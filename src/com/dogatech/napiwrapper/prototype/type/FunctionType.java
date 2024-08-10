package com.dogatech.napiwrapper.prototype.type;

import com.dogatech.napiwrapper.grammar.napiwrapperParser;
import com.dogatech.napiwrapper.io.Outputter;
import com.dogatech.napiwrapper.prototype.CppMethod;


public class FunctionType extends CppType {
  // This is specific, but I'm not sure of a better way at this time to distinguish
  // between the different type of callbacks. AI will be doing this job in the future anyway.
  public boolean isAlertCallback = false;

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
    if (isAlertCallback)
      outputUnwrapTheadSafe(from, to);
    else if (generics.size() == 1 && generics.get(0).name.equals("float"))
      outputUnwrapAsyncProgress(from, to);
    else
      outputUnwrapErrorCallback(from, to);
  }

  private void outputUnwrapAsyncProgress(String from, String to) {
    o.i().p("Napi::Function " + to + " = " + from + ".As<Napi::Function>();");
  }

  private void outputUnwrapErrorCallback(String from, String to) {
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

  private void outputUnwrapTheadSafe(String from, String to) {
    // First unwrap into a Napi::ThreadSafeFunction
    String f = to + "Fn";
    o.i().p("Napi::ThreadSafeFunction " + f + " = Napi::ThreadSafeFunction::New(env, " + from + ".As<Napi::Function>(), \"Callback\", 0, 1);");
    // Next create a normal callback function to pass to the method
    o.i().p("auto " + to + " = [" + f + "](", false);
    for (int i = 0; i < generics.size(); ++i) {
      CppType t = generics.get(i);
      if (i > 0) o.p(", ", false);
      o.p(t.fullName(), false);
      o.p(" p" + i, false);
    }
    o.p(") {");
    o.incIndent();
    // Now create a v8 callback to pass to the ThreadSafeFunction
    o.i().p("auto callback = [", false);
    for (int i = 0; i < generics.size(); ++i) {
      if (i > 0) o.p(", ", false);
      o.p("p" + i, false);
    }
    o.p("](Napi::Env env, Napi::Function jsCallback) {");
    o.incIndent().i().p("jsCallback.Call({", false);
    for (int i = 0; i < generics.size(); ++i) {
      if (i > 0) o.p(", ");
      CppType t = generics.get(i);
      t.outputWrap("p" + i);
    }
    o.p("});");
    o.decIndent().i().p("};");
    // Finally call the callback which calls our v8 callback inside this c callback
    o.i().p(f + ".BlockingCall(callback);");
    o.decIndent().i().p("};");
  }
}