package com.dogatech.nodewebkitwrapper.prototype.type;

import com.dogatech.nodewebkitwrapper.grammar.nodewebkitwrapperParser;
import com.dogatech.nodewebkitwrapper.io.Outputter;
import com.dogatech.nodewebkitwrapper.prototype.CppClass;
import com.dogatech.nodewebkitwrapper.prototype.CppMethod;


public abstract class CppType {
  protected boolean isConst;
  protected boolean isPointer;
  protected boolean isReference;
  public String name;
  protected CppClass cppClass;
  protected Outputter o;

  /** Returns true if this object can handle the given type string */
  public abstract boolean isType(String name);

  /** Writes to the Outputter how this object should be returned from a call. */
  public void outputResult() {
    o.i().p((isConst ? "const " : "") + nameSansRef() + " result =", false);
  }

  /** Writes to the Outputter how this type should be wrapped are returned. */
  public void outputReturn() {
    o.i().p("NanReturnValue(", false);
    outputWrap("result");
    o.p(");");
  }

  /** Returns a string for how this object is wrapped. */
  public void outputWrap(String var) {
    o.p("/* not implemented */", false);
  }

  public void outputUnwrap(String from, String to) {
    o.i().p("/* not implemented */");
  }

  public String[] requiredHeaders() {
    // normally not needed
    return new String[] {};
  }

  protected void init(String n, CppClass c, Outputter out) {
    cppClass = c;
    name = n;
    isPointer = name.charAt(name.length() - 1) == '*';
    isReference = name.charAt(name.length() - 1) == '&';

    o = out;
  }

  protected String nameSansRef() {
    return isReference ? name.substring(0, name.length()-1) : name;
  }

  public String getGeneric() {
    return name.substring(name.indexOf("<") + 1, name.lastIndexOf(">"));
  }
}