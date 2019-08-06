package com.dogatech.nodewebkitwrapper.listener;

import java.io.IOException;
import java.io.OutputStream;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.LinkedList;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.antlr.v4.runtime.TokenStream;
import org.antlr.v4.runtime.misc.Interval;
import org.antlr.v4.runtime.misc.NotNull;

import com.dogatech.nodewebkitwrapper.grammar.nodewebkitwrapperBaseListener;
import com.dogatech.nodewebkitwrapper.grammar.nodewebkitwrapperParser;
import com.dogatech.nodewebkitwrapper.io.Outputter;
import com.dogatech.nodewebkitwrapper.prototype.CppClass;
import com.dogatech.nodewebkitwrapper.prototype.CppNamespace;
import com.dogatech.nodewebkitwrapper.prototype.CppMethod;
import com.dogatech.nodewebkitwrapper.prototype.type.CppType;


public class SourceWrapperListener extends nodewebkitwrapperBaseListener {
  nodewebkitwrapperParser parser;
  Outputter o;
  CppNamespace cppNamespace = new CppNamespace();
  CppClass cppClass;
  Set<String> classes = new HashSet<String>();

  public SourceWrapperListener(nodewebkitwrapperParser p, Outputter out) {
    parser = p;
    o = out;
  }

  @Override public void exitCppClass(@NotNull nodewebkitwrapperParser.CppClassContext ctx) {
    o.i().p("#include <iostream>");
    o.i().p("#include <node.h>");
    o.i().p("#include <nan.h>");
    o.i().p("#include \"" + cppClass.name + "_wrap.h\"");
    SortedSet<String> headers = new TreeSet<String>();
    for (CppMethod m : cppClass.methods.values()) {
      if (m.broken) continue;
      headers.addAll(m.returnType.requiredHeaders());
      for (CppType t : m.args) {
        headers.addAll(t.requiredHeaders());
      }
    }
    for (String h : headers) {
      o.i().p("#include \"" + h + "\"");
    }
    o.p("");
    o.i().p("Nan::Persistent<v8::Function> " + cppClass.name + "::constructor;");
    o.p("");
    o.p(cppClass.name + "::" + cppClass.name + "() : Nan::ObjectWrap(), " + cppClass.name.toLowerCase() + "(NULL), ownWrappedObject(true) {};");
    o.p(cppClass.name + "::" + cppClass.name + "(" + cppNamespace + cppClass.name + "* o) : Nan::ObjectWrap(), " + cppClass.name.toLowerCase() + "(o), ownWrappedObject(true) {};");
    o.p(cppClass.name + "::~" + cppClass.name + "() { if (ownWrappedObject) delete " + cppClass.name.toLowerCase() + "; };");
    o.p("");
    o.i().p("void " + cppClass.name + "::setNwcpValue(" + cppNamespace + cppClass.name + "* v, bool own) {").incIndent();
    o.i().p("if (ownWrappedObject)").incIndent();
    o.i().p("delete " + cppClass.name.toLowerCase() + ";").decIndent();
    o.i().p(cppClass.name.toLowerCase() + " = v;");
    o.i().p("ownWrappedObject = own;");
    o.decIndent().i().p("}");
    o.p("");
    o.i().p("void " + cppClass.name + "::New(const Nan::FunctionCallbackInfo<v8::Value>& info) {").incIndent();
    if (cppClass.isSingleton() || !cppClass.hasCopyCtor) {
      o.i().p("" + cppClass.name + "* obj = new " + cppClass.name + "(" + cppClass.createNewPointer() + ");");
    } else {
      o.i().p("" + cppClass.namespace + cppClass.name + "* wrappedObj = NULL;");
      o.i().p("if (info.Length()) {").incIndent();
      o.i().p("" + cppClass.namespace + cppClass.name + "* xtmp(Nan::ObjectWrap::Unwrap<" + cppClass.name + ">(info[0]->ToObject())->getNwcpValue());");
      o.i().p("" + cppClass.namespace + cppClass.name + "& x = *xtmp;");
      o.i().p("wrappedObj = new " + cppClass.namespace + cppClass.name + "(x);");
      o.decIndent().i().p("} else {").incIndent();
      o.i().p("wrappedObj = " + cppClass.createNewPointer() + ";");
      o.decIndent().i().p("}");
      o.p("");
      o.i().p("" + cppClass.name + "* obj = new " + cppClass.name + "(wrappedObj);");
    }
    o.i().p("obj->Wrap(info.This());");
    o.p("");
    o.i().p("info.GetReturnValue().Set(info.This());");
    o.decIndent().i().p("}");
    o.p("");
    o.i().p("v8::Local<v8::Object> " + cppClass.name + "::NewInstance() {").incIndent();
    o.i().p("v8::Local<v8::Function> cons = Nan::New<v8::Function>(constructor);");
    o.i().p("return Nan::NewInstance(cons).ToLocalChecked();");
    o.decIndent().i().p("}");
    o.p("");
    o.i().p("void " + cppClass.name + "::Init(v8::Local<v8::Object> exports) {").incIndent();
    o.i().p("// Prepare constructor template");
    o.i().p("v8::Local<v8::FunctionTemplate> tpl = Nan::New<v8::FunctionTemplate>(New);");
    o.i().p("tpl->SetClassName(Nan::New(\"" + cppClass.name + "\").ToLocalChecked());");
    o.i().p("tpl->InstanceTemplate()->SetInternalFieldCount(1);");
    o.p("");
    o.i().p("// Prototype");
    for (CppMethod m : cppClass.methods.values()) {
      m.outputDeclaration();
    }
    o.p("");
    o.i().p("constructor.Reset(Nan::GetFunction(tpl).ToLocalChecked());");
    o.i().p("exports->Set(Nan::New<v8::String>(\"" + cppClass.name + "\").ToLocalChecked(), Nan::GetFunction(tpl).ToLocalChecked());");
    o.decIndent().i().p("}");
    o.p("");
    for (CppMethod m : cppClass.methods.values()) {
      m.outputSource(cppNamespace.toString(), cppClass);
    }
  }

  @Override public void enterConstructor(@NotNull nodewebkitwrapperParser.ConstructorContext ctx) {
    if (ctx.parameterList().parameter().size() == 1 && ctx.parameterList().parameter().get(0).type().Identifier().toString().equals(cppClass.name)) {
      cppClass.hasCopyCtor = true;
    }
  }

  @Override public void enterMethod(@NotNull nodewebkitwrapperParser.MethodContext ctx) {
    cppClass.addMethod(new CppMethod(cppClass, ctx, o));
  }

  @Override
  public void enterNamespace(@NotNull nodewebkitwrapperParser.NamespaceContext ctx) {
    cppNamespace.push(ctx.Identifier().toString());
  }

  @Override
  public void exitNamespace(@NotNull nodewebkitwrapperParser.NamespaceContext ctx) {
    cppNamespace.pop();
  }

  @Override
  public void enterCppClass(@NotNull nodewebkitwrapperParser.CppClassContext ctx) {
    cppClass = new CppClass(cppNamespace, ctx);
  }

}
