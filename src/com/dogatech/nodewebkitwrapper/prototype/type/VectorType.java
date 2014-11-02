package com.dogatech.nodewebkitwrapper.prototype.type;

import java.util.ArrayList;

import com.dogatech.nodewebkitwrapper.grammar.nodewebkitwrapperParser;
import com.dogatech.nodewebkitwrapper.io.Outputter;


public class VectorType extends CppType { //TODO

  @Override
  public boolean isType(String name) {
    return name.startsWith("vector");
  }

  @Override
  public boolean isType(nodewebkitwrapperParser.TypeContext ctx) {
    return isType(ctx.Identifier().toString()) && ctx.Modifier().size() <= 1;
  }

  @Override
  public void outputResult() {
    o.i().p((isConst ? "const " : "") + fullName() + " result =", false);
  }

  @Override
  public void outputReturn() {
    o.i().p("v8::Handle<v8::Array> a = NanNew<v8::Array>((int) result" + (isPointer() ? "->" : ".") + "size());");
    o.i().p("for (int i = 0; i < (int) result" + (isPointer() ? "->" : ".") + "size(); i++) {").incIndent();
    generics.get(0).outputWrap("(" + (isPointer() ? "*" : "") + "result)[i]", true);
    o.i().p("a->Set(NanNew<v8::Number>(i), instance);").decIndent();
    o.i().p("}");
    if (isPointer()) o.i().p("delete result;");
    o.i().p("NanReturnValue(a);");
  }

  @Override
  public void outputUnwrap(String from, String to) {
    String a = to + "Array";
    o.i().p("v8::Local<v8::Array> " + a + " = v8::Local<v8::Array>::Cast(" + from + ");");
    o.i().p(fullName() + " " + to + ";");
    o.i().p("for (int i = 0; i < " + a + "->Length(); ++i) {").incIndent();
    o.i().p("v8::Local<v8::Value> tmp = " + a + "->Get(i);");
    generics.get(0).outputUnwrap("tmp", "x");
    o.i().p(to + (isPointer() ? "->" : ".") + "push_back(x);");
    o.decIndent().i().p("}");
  }

  @Override
  public String fullName() {
    StringBuilder sb = new StringBuilder();
    sb.append("std::vector<");
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