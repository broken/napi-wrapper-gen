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
  public String v8Type() {
    return "v8::Array";
  }

  @Override
  public void outputResult() {
    o.i().p("dogatech::ResultSetIterator<dogatech::soulsifter::" + cppClass.name + ">* result =", false);
  }

  @Override
  public void outputReturn() {
    String generic = this.generics.get(0).name;  // we only expect one
    o.i().p("vector<dogatech::soulsifter::" + cppClass.name + "*>* v = result->toVector();");
    o.i().p("v8::Local<v8::Array> a = Nan::New<v8::Array>((int) v->size());");
    o.i().p("for (int i = 0; i < (int) v->size(); i++) {").incIndent();
    o.i().p("v8::Local<v8::Function> cons = Nan::New<v8::Function>(constructor);");
    o.i().p("v8::Local<v8::Object> instance = Nan::NewInstance(cons).ToLocalChecked();");
    o.i().p(generic + "* o = Nan::ObjectWrap::Unwrap<" + generic + ">(instance);");
    o.i().p("o->" + cppClass.name.toLowerCase() + " = (*v)[i];");
    o.i().p("a->Set(Nan::New<v8::Number>(i), instance);").decIndent();
    o.i().p("}");
    o.i().p("delete v;");
    o.i().p("info.GetReturnValue().Set(a);");
  }

  @Override
  public Set<String> requiredHeaders() {
    Set<String> s = super.requiredHeaders();
    s.add("ResultSetIterator.h");
    return s;
  }
}