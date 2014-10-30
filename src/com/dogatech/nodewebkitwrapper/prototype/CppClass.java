package com.dogatech.nodewebkitwrapper.prototype;

import java.util.ArrayList;
import java.util.List;

import com.dogatech.nodewebkitwrapper.grammar.nodewebkitwrapperParser;


public class CppClass {
  public String name;
  public final CppNamespace namespace;
  public List<CppMethod> methods = new ArrayList<CppMethod>();

  public CppClass(CppNamespace ns, nodewebkitwrapperParser.CppClassContext ctx) {
    name = ctx.Identifier().toString();
    namespace = new CppNamespace(ns.toString());
  }

  public String createNewPointer() {
    boolean isSingleton = false;
    for (CppMethod m : methods) {
      if (m.name.equals("getInstance")) {
        isSingleton = true;
        break;
      }
    }

    return isSingleton ? "&(" + namespace + name + "::getInstance())"
                       : "new " + namespace + name + "()";
  }
}