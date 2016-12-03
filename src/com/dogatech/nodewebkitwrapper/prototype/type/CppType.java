package com.dogatech.nodewebkitwrapper.prototype.type;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.dogatech.nodewebkitwrapper.grammar.nodewebkitwrapperParser;
import com.dogatech.nodewebkitwrapper.io.Outputter;
import com.dogatech.nodewebkitwrapper.prototype.CppClass;
import com.dogatech.nodewebkitwrapper.prototype.CppMethod;


public abstract class CppType {
  protected boolean isConst;
  public String name;
  List<CppType> generics = new ArrayList<CppType>();
  List<String> modifiers = new ArrayList<String>();
  protected CppClass cppClass;
  protected Outputter o;

  /** Returns true if this object can handle the given type string */
  public abstract boolean isType(String name);

  /** Returns the v8 type name of this type */
  public abstract String v8Type();

  /** Returns true if this object can handle the given type string */
  public boolean isType(nodewebkitwrapperParser.TypeContext ctx) {
    return isType(ctx.Identifier().toString());
  }

  /** Writes to the Outputter how this object should be returned from a call. */
  public void outputResult() {
    o.i().p((isConst ? "const " : "") + name + " result =", false);
  }

  /** Writes to the Outputter how this type should be wrapped are returned. */
  public void outputReturn() {
    o.i().p("info.GetReturnValue().Set(", false);
    outputWrap("result");
    o.p(");");
  }

  /** Returns a string for how this object is wrapped. */
  public void outputWrap(String var) {
    o.p("Nan::New<" + v8Type() + ">(" + var + ")", false);
  }

  public void outputWrap(String var, boolean own) {
    outputWrap(var);
  }

  public void outputUnwrap(String from, String to) {
    o.i().p("/* not implemented */");
  }

  public Set<String> requiredHeaders() {
    Set<String> s = new HashSet<String>();
    for (CppType t : generics) {
      s.addAll(t.requiredHeaders());
    }
    return s;
  }

  public String fullName() {
    return name;
  }

  public boolean isPointer() {
    return modifiers.size() > 0 && modifiers.get(0).equals("*");
  }

  public boolean isReference() {
    return modifiers.size() > 0 && modifiers.get(0).equals("&");
  }

  protected void init(String n, CppClass c, Outputter out) {
    cppClass = c;
    name = n;
    o = out;
  }
}