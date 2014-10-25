package com.dogatech.nodewebkitwrapper.prototype;

import java.util.Iterator;
import java.util.Stack;


public class CppNamespace {
  private Stack<String> namespaces = new Stack<String>();
  private String namespace = "";
  private void setNamespace() {
    Iterator<String> it = namespaces.iterator();
    StringBuilder sb = new StringBuilder();
    while (it.hasNext()) {
        sb.append(it.next()).append("::");
    }
    namespace = sb.toString();
  }
  public void push(String ns) {
    namespaces.push(ns);
    setNamespace();
  }
  public void pop() {
    namespaces.pop();
    setNamespace();
  }
  public String toString() {
    return namespace;
  }
}