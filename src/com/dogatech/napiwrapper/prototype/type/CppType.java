package com.dogatech.napiwrapper.prototype.type;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.dogatech.napiwrapper.grammar.napiwrapperParser;
import com.dogatech.napiwrapper.io.Outputter;
import com.dogatech.napiwrapper.prototype.CppClass;
import com.dogatech.napiwrapper.prototype.CppMethod;


public abstract class CppType {
  protected boolean isConst;
  public String name;
  public List<CppType> generics = new ArrayList<CppType>();
  List<String> modifiers = new ArrayList<String>();
  protected CppClass cppClass;
  protected Outputter o;

  /** Returns true if this object can handle the given type string */
  public abstract boolean isType(String name);

  /** Returns true if this object can handle the given type string */
  public boolean isType(napiwrapperParser.TypeContext ctx) {
    return isType(ctx.Identifier().toString());
  }

  /** Writes to the Outputter how this object should accept a return from a call. */
  public void outputResult() {
    o.i().p(fullName(false) + " result =", false);
  }

  /** Writes to the Outputter how this type should be wrapped and returned. */
  public void outputReturn() {
    o.i().p("return ", false);
    outputWrap("result");
    o.p(";");
  }

  /** Returns a string for how this object is wrapped. */
  public void outputWrap(String var) {
    o.i().p("/* not implemented */");
  }

  public void outputWrap(String var, boolean own) {
    outputWrap(var);
  }

  public void outputWrap(String var, String to) {
    o.i().p("auto " + to + " = ", false);
    outputWrap(var);
    o.p(";");
  }

  /** Returns a string for how this object is unwrapped. MethodType is used for errors. */
  public void outputUnwrap(String from, String to, CppMethod.MethodType mt) {
    o.i().p("/* not implemented */");
  }

  /** Returns a set of header files required for this type. */
  public Set<String> requiredHeaders() {
    Set<String> s = new HashSet<String>();
    for (CppType t : generics) {
      s.addAll(t.requiredHeaders());
    }
    return s;
  }

  /** Returns the full name of this type including generics & modifiers. */
  public String fullName() {
    return fullName(false);
  }

  public String fullName(boolean rmMod) {
    StringBuilder sb = new StringBuilder();
    if (isConst) sb.append("const ");
    sb.append(name);
    if (generics.size() > 0) {
      sb.append("<");
      for (int i = 0; i < generics.size(); ++i) {
        if (i > 0) sb.append(", ");
        sb.append(generics.get(i).fullName());
      }
      sb.append(">");
    }
    if (!rmMod) {
      for (String m : modifiers) {
        sb.append(m);
      }
    }
    return sb.toString();
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