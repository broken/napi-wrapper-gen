package com.dogatech.nodewebkitwrapper.prototype.type;

import com.dogatech.nodewebkitwrapper.grammar.nodewebkitwrapperParser;
import com.dogatech.nodewebkitwrapper.io.Outputter;


public class VectorType extends CppType {

  @Override
  public boolean isType(String name) {
    return name.startsWith("vector<");
  }

  @Override
  public void outputReturn() {
    CppType generic = CppTypeFactory.instance().createType(getGeneric(), cppClass, o);
    o.i().p("v8::Handle<v8::Array> a = NanNew<v8::Array>((int) result" + (isPointer ? "->" : ".") + "size());");
    o.i().p("for (int i = 0; i < (int) result" + (isPointer ? "->" : ".") + "size(); i++) {").incIndent();
    o.i();
    generic.outputWrap("result[i]");
    o.p("");
    o.i().p("v8::Local<v8::Object> instance = " + name.replaceAll("(&|\\*)", "") + "::NewInstance();");
    o.i().p(name.replaceAll("(&|\\*)", "") + (name.endsWith("*") ? "*" : "") + " o = ObjectWrap::Unwrap<" + name.replaceAll("(&|\\*)", "") + ">(instance);");
    o.i().p("o->setNwcpValue((" + (isPointer ? "*" : "") + "result)[i]);");
    o.i().p("a->Set(NanNew<v8::Number>(i), instance);").decIndent();

    o.i().p("}");
    if (isPointer) o.i().p("  delete result;");
    o.i().p("NanReturnValue(a);");

    o.i().p("NanReturnValue(NanNew<v8::String>(result.c_str(), result.length()));");
  }
}