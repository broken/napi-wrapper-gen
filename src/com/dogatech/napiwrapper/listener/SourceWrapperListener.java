package com.dogatech.napiwrapper.listener;

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

import com.dogatech.napiwrapper.grammar.napiwrapperBaseListener;
import com.dogatech.napiwrapper.grammar.napiwrapperParser;
import com.dogatech.napiwrapper.io.Outputter;
import com.dogatech.napiwrapper.listener.BaseWrapperListener;
import com.dogatech.napiwrapper.prototype.CppClass;
import com.dogatech.napiwrapper.prototype.CppNamespace;
import com.dogatech.napiwrapper.prototype.CppMethod;
import com.dogatech.napiwrapper.prototype.type.CppType;


public class SourceWrapperListener extends BaseWrapperListener {
  Set<String> classes = new HashSet<String>();

  public SourceWrapperListener(napiwrapperParser p, Outputter out) {
    super(p, out);
  }

  @Override public void exitCppClass(@NotNull napiwrapperParser.CppClassContext ctx) {
    o.i().p("#include <napi.h>");
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
    o.i().p("Napi::FunctionReference* " + cppClass.name + "::constructor = nullptr;");
    o.p("");
    o.p(cppClass.name + "::~" + cppClass.name + "() { if (ownWrappedObject) delete " + cppClass.name.toLowerCase() + "; };");
    o.p("");
    o.i().p("void " + cppClass.name + "::setWrappedValue(" + cppNamespace + cppClass.name + "* v, bool own) {").incIndent();
    o.i().p("if (ownWrappedObject)").incIndent();
    o.i().p("delete " + cppClass.name.toLowerCase() + ";").decIndent();
    o.i().p(cppClass.name.toLowerCase() + " = v;");
    o.i().p("ownWrappedObject = own;");
    o.decIndent().i().p("}");
    o.p("");
    o.i().p("Napi::Object " + cppClass.name + "::Init(Napi::Env env, Napi::Object exports) {").incIndent();
    o.i().p("Napi::Function func = DefineClass(env, \"" + cppClass.name + "\", {").incIndent();
    for (CppMethod m : cppClass.methods.values()) {
      m.outputDeclaration();
    }
    o.decIndent().i().p("});");
    o.p("");
    o.i().p("constructor = new Napi::FunctionReference();");
    o.i().p("*constructor = Napi::Persistent(func);");
    o.p("");
    o.i().p("exports.Set(\"" + cppClass.name + "\", func);");
    o.i().p("return exports;");
    o.decIndent().i().p("}");
    o.p("");
    o.i().p("Napi::Object " + cppClass.name + "::NewInstance(Napi::Env env) {").incIndent();
    o.i().p("Napi::EscapableHandleScope scope(env);");
    o.i().p("Napi::Object obj = " + cppClass.name + "::constructor->New({});");
    o.i().p("return scope.Escape(napi_value(obj)).ToObject();");
    o.decIndent().i().p("}");
    o.p("");
    o.i().p(cppClass.name + "::" + cppClass.name + "(const Napi::CallbackInfo& info) : Napi::ObjectWrap<" + cppClass.name + ">(info), " + cppClass.name.toLowerCase() + "(nullptr), ownWrappedObject(" + !cppClass.isSingleton() + ") {").incIndent();
    if (!cppClass.hasCopyCtor) {
      o.i().p(cppClass.name.toLowerCase() + " = " + cppClass.createNewPointer() + ";");
    } else {
      o.i().p("if (info.Length()) {").incIndent();
      o.i().p("" + cppClass.namespace + cppClass.name + "* x = Napi::ObjectWrap<" + cppClass.name + ">::Unwrap(info[0].As<Napi::Object>())->getWrappedValue();");
      o.i().p(cppClass.name.toLowerCase() + " = new " + cppClass.namespace + cppClass.name + "(*x);");
      o.decIndent().i().p("} else {").incIndent();
      o.i().p(cppClass.name.toLowerCase() + " = " + cppClass.createNewPointer() + ";");
      o.decIndent().i().p("}");
    }
    o.decIndent().i().p("}");
    o.p("");
    for (CppMethod m : cppClass.methods.values()) {
      m.outputSource(cppNamespace.toString(), cppClass);
    }
  }

}
