package com.dogatech.nodewebkitwrapper.listener;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Iterator;
import java.util.Stack;

import org.antlr.v4.runtime.TokenStream;
import org.antlr.v4.runtime.misc.Interval;
import org.antlr.v4.runtime.misc.NotNull;

import com.dogatech.nodewebkitwrapper.grammar.nodewebkitwrapperBaseListener;
import com.dogatech.nodewebkitwrapper.grammar.nodewebkitwrapperParser;
import com.dogatech.nodewebkitwrapper.io.Outputter;
import com.dogatech.nodewebkitwrapper.listener.BaseWrapperListener;
import com.dogatech.nodewebkitwrapper.prototype.CppClass;
import com.dogatech.nodewebkitwrapper.prototype.CppNamespace;
import com.dogatech.nodewebkitwrapper.prototype.CppMethod;
import com.dogatech.nodewebkitwrapper.prototype.type.CppType;


public class HeaderWrapperListener extends BaseWrapperListener {

  public HeaderWrapperListener(nodewebkitwrapperParser p, Outputter out) {
    super(p, out);
  }

  @Override public void exitCppClass(@NotNull nodewebkitwrapperParser.CppClassContext ctx) {
    o.i().p("#ifndef " + cppClass.name + "_wrap_h");
    o.i().p("#define " + cppClass.name + "_wrap_h");
    o.p("");
    o.i().p("#include <napi.h>");
    o.i().p("#include \"" + cppClass.name + ".h\"");
    o.p("");
    o.i().p("class " + cppClass.name + " : public Napi::ObjectWrap<" + cppClass.name + "> {");
    o.i().p(" public:").incIndent();
    o.i().p("static Napi::Object Init(Napi::Env env, Napi::Object exports);");
    o.i().p("static Napi::Object NewInstance(Napi::Env env);");
    o.i().p(cppClass.name + "(const Napi::CallbackInfo& info);");
    o.i().p("~" + cppClass.name + "();");
    o.p("");
    o.i().p("void setWrappedValue(" + cppNamespace + cppClass.name + "* v, bool own);");
    o.i().p(cppNamespace + cppClass.name + "* getWrappedValue() const { return " + cppClass.name.toLowerCase() + "; }");
    o.p("").decIndent();
    o.i().p(" private:").incIndent();
    for (CppMethod m : cppClass.methods.values()) {
      m.outputHeader();
    }
    o.p("");
    o.i().p("static Napi::FunctionReference* constructor;");
    o.i().p(cppNamespace + cppClass.name + "* " + cppClass.name.toLowerCase() + ";");
    o.i().p("bool ownWrappedObject;");
    o.decIndent();
    o.i().p("};");
    o.p("");
    o.i().p("#endif");
  }

}