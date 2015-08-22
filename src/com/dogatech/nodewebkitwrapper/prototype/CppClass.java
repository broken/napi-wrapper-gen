package com.dogatech.nodewebkitwrapper.prototype;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import com.dogatech.nodewebkitwrapper.grammar.nodewebkitwrapperParser;


public class CppClass {
  public String name;
  public final CppNamespace namespace;
  public Map<String, CppMethod> methods = new LinkedHashMap<String, CppMethod>();
  Set<String> getters = new HashSet<String>();
  Set<String> setters = new HashSet<String>();

  public CppClass(CppNamespace ns, nodewebkitwrapperParser.CppClassContext ctx) {
    name = ctx.Identifier().toString();
    namespace = new CppNamespace(ns.toString());
  }

  public String createNewPointer() {
    return isSingleton() ? "&(" + namespace + name + "::getInstance())"
                       : "new " + namespace + name + "()";
  }

  public void addMethod(CppMethod m) {
    if (methods.get(m.name) == null) methods.put(m.name, m);
    if (m.isGetter) getters.add(m.accessor());
    if (m.isSetter) setters.add(m.accessor());
  }

  public boolean isSingleton() {
    boolean isSingleton = false;
    for (CppMethod m : methods.values()) {
      if (m.name.equals("getInstance")) {
        isSingleton = true;
        break;
      }
    }
    return isSingleton;
  }
}
