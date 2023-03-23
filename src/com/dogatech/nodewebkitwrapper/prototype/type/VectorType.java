package com.dogatech.nodewebkitwrapper.prototype.type;

import java.util.ArrayList;

import com.dogatech.nodewebkitwrapper.grammar.nodewebkitwrapperParser;
import com.dogatech.nodewebkitwrapper.io.Outputter;
import com.dogatech.nodewebkitwrapper.prototype.CppMethod;


public class VectorType extends CppType { //TODO

  @Override
  public boolean isType(String name) {
    return name.startsWith("vector") || name.startsWith("std::vector");
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
  public void outputWrap(String from, String to) {
    o.i().p("Napi::Array " + to + " = Napi::Array::New(env, static_cast<int>(" + from + (isPointer() ? "->" : ".") + "size()));");
    o.i().p("for (int i = 0; i < (int) " + from + (isPointer() ? "->" : ".") + "size(); i++) {").incIndent();
    CppType t = generics.get(0);
    if (t instanceof SoulSifterModelType) {  // TODO should be generic model
      t.outputWrap("(" + (isPointer() ? "*" : "") + from + ")[i]", !isReference());
      o.i().p(to + ".Set(i, instance);").decIndent();
    } else {
      o.i().p(to + ".Set(i, ", false);
      t.outputWrap((isPointer() ? "*" : "") + from + "[i]");
      o.p(");").decIndent();
    }
    o.i().p("}");
    if (isPointer()) o.i().p("delete " + from + ";");
  }

  @Override
  public void outputReturn() {
    outputWrap("result", "a");
    o.i().p("return a;");
  }

  @Override
  public void outputUnwrap(String from, String to, CppMethod.MethodType mt) {
    o.i().p("if (!" + from + ".IsArray()) {").incIndent();
    mt.errOut("TypeError: Array expected (for " + from + ")");
    o.decIndent().i().p("}");
    String a = to + "Array";
    o.i().p("Napi::Array " + a + " = " + from + ".As<Napi::Array>();");
    o.i().p(fullName() + " " + to + ";");
    o.i().p("for (uint32_t i = 0; i < " + a + ".Length(); ++i) {").incIndent();
    generics.get(0).outputUnwrap(a + ".Get(i)", "x", mt);
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