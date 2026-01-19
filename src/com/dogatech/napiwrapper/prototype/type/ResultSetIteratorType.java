package com.dogatech.napiwrapper.prototype.type;

import java.util.Set;

import com.dogatech.napiwrapper.grammar.napiwrapperParser;
import com.dogatech.napiwrapper.io.Outputter;


public class ResultSetIteratorType extends CppType {

  @Override
  public boolean isType(String name) {
    return name.startsWith("ResultSetIterator");
  }

  @Override
  public void outputResult() {
    o.i().p("dogatech::ResultSetIterator<dogatech::soulsifter::" + cppClass.name + ">* result =", false);
  }

  @Override
  public void outputReturn() {
    String generic = this.generics.get(0).name;  // we only expect one
    o.i().p("if (result == nullptr) return Napi::Array::New(env, 0);");
    o.i().p("vector<dogatech::soulsifter::" + cppClass.name + "*>* v = result->toVector();");
    o.i().p("Napi::Array a = Napi::Array::New(env, static_cast<int>(v->size()));");
    o.i().p("for (int i = 0; i < (int) v->size(); i++) {").incIndent();
    o.i().p("Napi::Object instance = " + generic + "::NewInstance(env);");
    o.i().p(generic + "* r = Napi::ObjectWrap<" + generic + ">::Unwrap(instance);");
    o.i().p("r->setWrappedValue((*v)[i], true);");
    o.i().p("a.Set(i, instance);").decIndent();
    o.i().p("}");
    o.i().p("delete v;");
    o.i().p("return a;");
  }

  @Override
  public Set<String> requiredHeaders() {
    Set<String> s = super.requiredHeaders();
    s.add("ResultSetIterator.h");
    return s;
  }
}