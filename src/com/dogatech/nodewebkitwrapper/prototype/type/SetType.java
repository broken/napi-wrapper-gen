package com.dogatech.nodewebkitwrapper.prototype.type;

import java.util.HashMap;

import com.dogatech.nodewebkitwrapper.grammar.nodewebkitwrapperParser;
import com.dogatech.nodewebkitwrapper.io.Outputter;


public class SetType extends VectorType {

  @Override
  public boolean isType(String name) {
    // TODO fix - currently nothing returns a set
    return false;
    // return name.startsWith("set");
  }

  @Override
  public void outputReturn() {  // TODO make this default for vector as well
    o.i().p("v8::Local<v8::Array> a = Nan::New<v8::Array>((int) result" + (isPointer() ? "->" : ".") + "size());");
    o.i().p("int idx = 0;");
    o.i().p("for (const auto& element : " + (isPointer() ? "*" : "") + "result) {").incIndent();
    CppType t = generics.get(0);
    if (t instanceof SoulSifterModelType) {  // TODO should be generic model
      t.outputWrap("element", !isReference());
      o.i().p("a->Set(Nan::GetCurrentContext(), Nan::New<v8::Number>(idx), instance);");
    } else {
      o.i().p("a->Set(Nan::GetCurrentContext(), Nan::New<v8::Number>(idx), ", false);
      t.outputWrap("element");
      o.p(");");
    }
    o.i().p("++idx;").decIndent();
    o.i().p("}");
    if (isPointer()) o.i().p("delete result;");  // TODO returning pointers should not be giving ownership
    o.i().p("info.GetReturnValue().Set(a);");
  }

  @Override
  public void outputUnwrap(String from, String to) {
    String a = to + "Array";
    o.i().p("v8::Local<v8::Array> " + a + " = v8::Local<v8::Array>::Cast(" + from + ");");
    o.i().p(fullName() + " " + to + ";");
    o.i().p("for (int i = 0; i < " + a + "->Length(); ++i) {").incIndent();
    o.i().p("v8::Local<v8::Value> tmp = " + a + "->Get(Nan::GetCurrentContext(), i).ToLocalChecked();");
    generics.get(0).outputUnwrap("tmp", "x");
    o.i().p(to + (isPointer() ? "->" : ".") + "insert(x);");
    o.decIndent().i().p("}");
  }

  @Override
  public String fullName() {
    StringBuilder sb = new StringBuilder();
    sb.append("std::set<");
    for (int i = 0; i < generics.size(); ++i) {
      CppType t = generics.get(i);
      if (i > 0) sb.append(", ");
      sb.append(t.fullName());
      for (String m : t.modifiers) {
        sb.append(m);
      }
    }
    sb.append(">");
    if (isPointer()) sb.append("*");
    return sb.toString();
  }
}
