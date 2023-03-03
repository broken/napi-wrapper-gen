package com.dogatech.nodewebkitwrapper.prototype.type;

import java.util.ArrayList;

import com.dogatech.nodewebkitwrapper.grammar.nodewebkitwrapperParser;
import com.dogatech.nodewebkitwrapper.io.Outputter;


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
  public void outputReturn() {
    o.i().p("Napi::Array a = Napi::Array::New(info.Env(), static_cast<int>(result" + (isPointer() ? "->" : ".") + "size()));");
    o.i().p("for (int i = 0; i < (int) result" + (isPointer() ? "->" : ".") + "size(); i++) {").incIndent();
    CppType t = generics.get(0);
    if (t instanceof SoulSifterModelType) {  // TODO should be generic model
      t.outputWrap("(" + (isPointer() ? "*" : "") + "result)[i]", !isReference());
      o.i().p("a.Set(i, instance);").decIndent();
    } else {
      o.i().p("a.Set(i, ", false);
      t.outputWrap((isPointer() ? "*" : "") + "result[i]");
      o.p(");").decIndent();
    }
    o.i().p("}");
    if (isPointer()) o.i().p("delete result;");
    o.i().p("return a;");
  }

  @Override
  public void outputUnwrap(String from, String to) {
    String a = to + "Array";
    o.i().p("Napi::Array " + a + " = " + from + ".As<Napi::Array>();");
    o.i().p(fullName() + " " + to + ";");
    o.i().p("for (uint32_t i = 0; i < " + a + ".Length(); ++i) {").incIndent();
    o.i().p("Napi::Value tmp = " + a + ".Get(i);");
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