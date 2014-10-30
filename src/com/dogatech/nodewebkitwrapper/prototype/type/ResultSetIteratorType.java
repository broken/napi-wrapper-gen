package com.dogatech.nodewebkitwrapper.prototype.type;

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
    o.i().p("vector<dogatech::soulsifter::" + cppClass.name + "*>* v = result->toVector();");
    o.i().p("v8::Handle<v8::Array> a = NanNew<v8::Array>((int) v->size());");
    o.i().p("for (int i = 0; i < (int) v->size(); i++) {").incIndent();
    o.i().p("v8::Local<v8::Function> cons = NanNew<v8::Function>(constructor);");
    o.i().p("v8::Local<v8::Object> instance = cons->NewInstance();");
    o.i().p(getGeneric() + "* o = ObjectWrap::Unwrap<" + getGeneric() + ">(instance);");
    o.i().p("o->" + cppClass.name.toLowerCase() + " = (*v)[i];");
    o.i().p("a->Set(NanNew<v8::Number>(i), instance);").decIndent();
    o.i().p("}");
    o.i().p("delete v;");
    o.i().p("NanReturnValue(a);");
  }
}