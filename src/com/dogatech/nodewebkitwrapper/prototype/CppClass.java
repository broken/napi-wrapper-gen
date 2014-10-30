package com.dogatech.nodewebkitwrapper.prototype;

import java.util.ArrayList;
import java.util.List;


public class CppClass {
  public String name;
  public final CppNamespace namespace;
  public List<CppMethod> methods = new ArrayList<CppMethod>();

  public CppClass(CppNamespace ns) {
    namespace = new CppNamespace(ns.toString());
  }
}