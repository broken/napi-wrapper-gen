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
    o.i().p("v8::Persistent<v8::Function> " + cppClass.name + "::constructor;");
    o.p("");
    o.p(cppClass.name + "::" + cppClass.name + "() : ObjectWrap(), " + cppClass.name.toLowerCase() + "(NULL), ownWrappedObject(true) {};");
    o.p(cppClass.name + "::" + cppClass.name + "(" + cppNamespace + cppClass.name + "* o) : ObjectWrap(), " + cppClass.name.toLowerCase() + "(o), ownWrappedObject(true) {};");
    o.p(cppClass.name + "::~" + cppClass.name + "() { if (ownWrappedObject) delete " + cppClass.name.toLowerCase() + "; };");
    o.p("");
    o.i().p("void " + cppClass.name + "::setNwcpValue(" + cppNamespace + cppClass.name + "* v, bool own) {").incIndent();
    o.i().p("if (ownWrappedObject)").incIndent();
    o.i().p("delete " + cppClass.name.toLowerCase() + ";").decIndent();
    o.i().p(cppClass.name.toLowerCase() + " = v;");
    o.i().p("ownWrappedObject = own;");
    o.decIndent().i().p("}");
    o.p("");
    o.i().p("NAN_METHOD(" + cppClass.name + "::New) {").incIndent();
    o.i().p("NanScope();");
    o.p("");
    if (cppClass.isSingleton() || !cppClass.hasCopyCtor) {
      o.i().p("" + cppClass.name + "* obj = new " + cppClass.name + "(" + cppClass.createNewPointer() + ");");
    } else {
      o.i().p("" + cppClass.namespace + cppClass.name + "* wrappedObj = NULL;");
      o.i().p("if (args.Length()) {").incIndent();
      o.i().p("" + cppClass.namespace + cppClass.name + "* xtmp(node::ObjectWrap::Unwrap<" + cppClass.name + ">(args[0]->ToObject())->getNwcpValue());");
      o.i().p("" + cppClass.namespace + cppClass.name + "& x = *xtmp;");
      o.i().p("wrappedObj = new " + cppClass.namespace + cppClass.name + "(x);");
      o.decIndent().i().p("} else {").incIndent();
      o.i().p("wrappedObj = " + cppClass.createNewPointer() + ";");
      o.decIndent().i().p("}");
      o.p("");
      o.i().p("" + cppClass.name + "* obj = new " + cppClass.name + "(wrappedObj);");
    }
    o.i().p("obj->Wrap(args.This());");
    o.p("");
    o.i().p("NanReturnValue(args.This());");
    o.decIndent().i().p("}");
    o.p("");
    o.i().p("v8::Local<v8::Object> " + cppClass.name + "::NewInstance() {").incIndent();
    o.i().p("v8::Local<v8::Function> cons = NanNew<v8::Function>(constructor);");
    o.i().p("v8::Local<v8::Object> instance = cons->NewInstance();");
    o.p("");
    o.i().p("return instance;");
    o.decIndent().i().p("}");
    o.p("");
    o.i().p("void " + cppClass.name + "::Init(v8::Handle<v8::Object> exports) {").incIndent();
    o.i().p("NanScope();");
    o.p("");
    o.i().p("// Prepare constructor template");
    o.i().p("v8::Local<v8::FunctionTemplate> tpl = NanNew<v8::FunctionTemplate>(New);");
    o.i().p("tpl->SetClassName(NanNew<v8::String>(\"" + cppClass.name + "\"));");
    o.i().p("tpl->InstanceTemplate()->SetInternalFieldCount(1);");
    o.p("");
    for (CppMethod m : cppClass.methods.values()) {
      m.outputDeclaration();
    }
    o.p("");
    o.i().p("NanAssignPersistent<v8::Function>(constructor, tpl->GetFunction());");
    o.i().p("exports->Set(NanNew<v8::String>(\"" + cppClass.name + "\"), tpl->GetFunction());");
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
