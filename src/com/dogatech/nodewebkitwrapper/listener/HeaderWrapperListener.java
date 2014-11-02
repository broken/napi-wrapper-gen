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
import com.dogatech.nodewebkitwrapper.prototype.CppClass;
import com.dogatech.nodewebkitwrapper.prototype.CppNamespace;
import com.dogatech.nodewebkitwrapper.prototype.CppMethod;
import com.dogatech.nodewebkitwrapper.prototype.type.CppType;


public class HeaderWrapperListener extends nodewebkitwrapperBaseListener {
  nodewebkitwrapperParser parser;
  Outputter o;
  CppNamespace cppNamespace = new CppNamespace();
  CppClass cppClass;

  public HeaderWrapperListener(nodewebkitwrapperParser p, Outputter out) {
    parser = p;
    o = out;
  }

  @Override public void exitCppClass(@NotNull nodewebkitwrapperParser.CppClassContext ctx) {
    o.i().p("#ifndef " + cppClass.name + "_wrap_h");
    o.i().p("#define " + cppClass.name + "_wrap_h");
    o.p("");
    o.i().p("#include <node.h>");
    o.i().p("#include <nan.h>");
    o.i().p("#include \"" + cppClass.name + ".h\"");
    o.p("");
    o.i().p("class " + cppClass.name + " : public node::ObjectWrap {");
    o.i().p(" public:").incIndent();
    o.i().p("static void Init(v8::Handle<v8::Object> exports);");
    o.i().p("static v8::Local<v8::Object> NewInstance();");
    o.p("");
    o.i().p("void setNwcpValue(" + cppNamespace + cppClass.name + "* v, bool own);");
    o.i().p(cppNamespace + cppClass.name + "* getNwcpValue() const { return " + cppClass.name.toLowerCase() + "; }");
    o.p("").decIndent();
    o.i().p(" private:").incIndent();
    o.i().p(cppClass.name + "();");
    o.i().p("explicit " + cppClass.name + "(" + cppNamespace + cppClass.name + "* " + cppClass.name.toLowerCase() + ");");
    o.i().p("~" + cppClass.name + "();");
    o.p("");
    o.i().p("static NAN_METHOD(New);");
    o.p("");
    for (CppMethod m : cppClass.methods.values()) {
      m.outputHeader();
    }
    o.p("");
    o.i().p("static v8::Persistent<v8::Function> constructor;");
    o.i().p(cppNamespace + cppClass.name + "* " + cppClass.name.toLowerCase() + ";");
    o.i().p("bool ownWrappedObject;");
    o.decIndent();
    o.i().p("};");
    o.p("");
    o.i().p("#endif");
  }

  @Override public void enterNamespace(@NotNull nodewebkitwrapperParser.NamespaceContext ctx) {
    cppNamespace.push(ctx.Identifier().toString());
  }

  @Override public void exitNamespace(@NotNull nodewebkitwrapperParser.NamespaceContext ctx) {
    cppNamespace.pop();
  }

  @Override public void enterCppClass(@NotNull nodewebkitwrapperParser.CppClassContext ctx) {
    cppClass = new CppClass(cppNamespace, ctx);
  }

  @Override public void enterMethod(@NotNull nodewebkitwrapperParser.MethodContext ctx) {
    cppClass.addMethod(new CppMethod(cppClass, ctx, o));
  }

}