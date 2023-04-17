package com.dogatech.napiwrapper.listener;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Iterator;
import java.util.Stack;

import org.antlr.v4.runtime.TokenStream;
import org.antlr.v4.runtime.misc.Interval;
import org.antlr.v4.runtime.misc.NotNull;

import com.dogatech.napiwrapper.grammar.napiwrapperBaseListener;
import com.dogatech.napiwrapper.grammar.napiwrapperParser;
import com.dogatech.napiwrapper.io.Outputter;
import com.dogatech.napiwrapper.prototype.CppClass;
import com.dogatech.napiwrapper.prototype.CppNamespace;
import com.dogatech.napiwrapper.prototype.CppMethod;
import com.dogatech.napiwrapper.prototype.type.CppType;


public class BaseWrapperListener extends napiwrapperBaseListener {
  napiwrapperParser parser;
  Outputter o;
  CppNamespace cppNamespace = new CppNamespace();
  CppClass cppClass;

  public BaseWrapperListener(napiwrapperParser p, Outputter out) {
    parser = p;
    o = out;
  }

  @Override public void enterNamespace(@NotNull napiwrapperParser.NamespaceContext ctx) {
    cppNamespace.push(ctx.Identifier().toString());
  }

  @Override public void exitNamespace(@NotNull napiwrapperParser.NamespaceContext ctx) {
    cppNamespace.pop();
  }

  @Override public void enterCppClass(@NotNull napiwrapperParser.CppClassContext ctx) {
    cppClass = new CppClass(cppNamespace, ctx);
  }

  @Override public void enterMethod(@NotNull napiwrapperParser.MethodContext ctx) {
    cppClass.addMethod(new CppMethod(cppClass, ctx, o));
  }

  @Override public void enterConstructor(@NotNull napiwrapperParser.ConstructorContext ctx) {
    if (ctx.parameterList().parameter().size() == 1 && ctx.parameterList().parameter().get(0).type(0).Identifier().toString().equals(cppClass.name)) {
      cppClass.hasCopyCtor = true;
    }
  }

}