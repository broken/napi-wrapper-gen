package com.dogatech.nodewebkitwrapper.prototype.type;

import java.util.Set;

import com.dogatech.nodewebkitwrapper.grammar.nodewebkitwrapperParser;
import com.dogatech.nodewebkitwrapper.io.Outputter;


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
    o.i().p("vector<dogatech::soulsifter::" + cppClass.name + "*>* v = result->toVector();");
    o.i().p("Napi::Array a = Napi::Array::New(info.Env(), static_cast<int>(v->size()));");
    o.i().p("for (int i = 0; i < (int) v->size(); i++) {").incIndent();
    o.i().p("Napi::Object instance = " + generic + "::NewInstance(info.Env());");
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