package com.dogatech.nodewebkitwrapper.listener;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Iterator;
import java.util.List;
import java.util.LinkedList;
import java.util.Set;
import java.util.HashSet;

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
  CppClass cppClass;
  CppNamespace cppNamespace = new CppNamespace();
  Outputter o;
  Set<String> classes = new HashSet<String>();
  Set<String> getters = new HashSet<String>();
  Set<String> setters = new HashSet<String>();

  public SourceWrapperListener(nodewebkitwrapperParser p, Outputter out) {
    parser = p;
    o = out;
  }

  private boolean isSingleton() {
    for (CppMethod m : cppClass.methods) {
      if (m.name.equals("getInstance")) {
        return true;
      }
    }
    return false;
  }

  public boolean skipMethod(CppMethod m) {
    boolean cannotHandleArg = false;
    /*for (CppType t : m.args) {
      cannotHandleArg |= t.isUnknownType(cppClass);
    }*/
    return cannotHandleArg;
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
    cppClass = new CppClass(cppNamespace);
    cppClass.name = ctx.Identifier().toString();
  }

  @Override public void exitCppClass(@NotNull nodewebkitwrapperParser.CppClassContext ctx) {
    o.p("#include <iostream>");
    o.p("#include <node.h>");
    o.p("#include <nan.h>");
    o.p("#include \"" + cppClass.name + ".h\"");
    o.p("#include \"" + cppClass.name + "_wrap.h\"");
    for (String n : classes) {
      o.p("#include \"" + n + ".h\"");
      o.p("#include \"" + n + "_wrap.h\"");
    }
    o.p("");
    o.p("v8::Persistent<v8::Function> " + cppClass.name + "::constructor;");
    o.p("");
    if (isSingleton()) {
      o.p(cppClass.name + "::" + cppClass.name + "() : ObjectWrap(), " + cppClass.name.toLowerCase() + "(&(" + cppNamespace + cppClass.name + "::getInstance())) {};");
    } else {
      o.p(cppClass.name + "::" + cppClass.name + "() : ObjectWrap(), " + cppClass.name.toLowerCase() + "(new " + cppNamespace + cppClass.name + "()) {};");
    }
    o.p(cppClass.name + "::" + cppClass.name + "(" + cppNamespace + cppClass.name + "* o) : ObjectWrap(), " + cppClass.name.toLowerCase() + "(o) {};");
    o.p(cppClass.name + "::~" + cppClass.name + "() { delete " + cppClass.name.toLowerCase() + "; };");
    o.p("");
    o.p("NAN_METHOD(" + cppClass.name + "::New) {").incIndent();
    o.i().p("NanScope();");
    o.p("");
    o.i().p("" + cppClass.name + "* obj = new " + cppClass.name + "();");
    o.i().p("obj->Wrap(args.This());");
    o.p("");
    o.i().p("NanReturnValue(args.This());");
    o.p("}");
    o.p("");
    o.p("v8::Local<v8::Object> " + cppClass.name + "::NewInstance() {");
    o.i().p("v8::Local<v8::Function> cons = NanNew<v8::Function>(constructor);");
    o.i().p("v8::Local<v8::Object> instance = cons->NewInstance();");
    o.p("");
    o.i().p("return instance;");
    o.p("}");
    o.p("");
    o.p("void " + cppClass.name + "::Init(v8::Handle<v8::Object> exports) {");
    o.i().p("NanScope();");
    o.p("");
    o.i().p("// Prepare constructor template");
    o.i().p("v8::Local<v8::FunctionTemplate> tpl = NanNew<v8::FunctionTemplate>(New);");
    o.i().p("tpl->SetClassName(NanNew<v8::String>(\"" + cppClass.name + "\"));");
    o.i().p("tpl->InstanceTemplate()->SetInternalFieldCount(1);");
    o.p("");
    for (CppMethod m : cppClass.methods) {
      m.outputDeclaration();
    }
    o.p("");
    o.i().p("NanAssignPersistent<v8::Function>(constructor, tpl->GetFunction());");
    o.i().p("exports->Set(NanNew<v8::String>(\"" + cppClass.name + "\"), tpl->GetFunction());");
    o.decIndent().i().p("}");
    o.p("");
    for (CppMethod m : cppClass.methods) {
      o.p("");
      m.outputSource(cppNamespace.toString(), cppClass);
    }
    o.p("");
  }

  @Override public void enterMethod(@NotNull nodewebkitwrapperParser.MethodContext ctx) {
    CppMethod m = new CppMethod(cppClass, ctx, o);
    if (skipMethod(m)) return;
    cppClass.methods.add(m);
    /*if (m.returnType.isVector) {
      String g = m.returnType.getGeneric();
      g = g.replaceAll("(&|\\*)", "");
      if (!g.equals(cppClass.name)) {
        classes.add(g);
      }
    } else if (m.returnType.isUnknownType(cppClass) && !m.returnType.isSet) {
      classes.add(m.returnType.name.replaceAll("(&|\\*)", ""));
    }*/
    for (CppType argType : m.args) {
      /*if (argType.isVector) {
        String g = argType.getGeneric();
        g = g.replaceAll("(&|\\*)", "");
        if (!g.equals(cppClass.name)) {
          classes.add(g);
        }
      }*/
    }
    if (m.isGetter) getters.add(m.accessor());
    if (m.isSetter) setters.add(m.accessor());
  }

}