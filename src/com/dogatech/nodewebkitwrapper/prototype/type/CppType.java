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

  public void outputUnwrap(String from, String to, String sp) {
    o.p("/* not implemented */", false);
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

  public String unwrap(String from, String to, String sp, String namespace, CppClass cppClass) {
    if (name.equals("int")) {
      return sp + "int " + to + "(" + from + "->Uint32Value());";
    } else if (name.equals("string&")) {
      return sp + "string " + to + "(" + "*v8::String::Utf8Value(" + from + "->ToString()));";
    } else if (name.equals("bool")) {
      return sp + "bool " + to + "(" + from + "->BooleanValue());";
    } else if (name.equals("time_t")) {
      return sp + "time_t " + to + "(" + from + "->Uint32Value() / 1000);";
    } else if (name.startsWith("vector<")) {
      StringBuilder sb = new StringBuilder();
      /*CppType generic = new CppType(getGeneric());
      sb.append(sp + "v8::Local<v8::Array> array = v8::Local<v8::Array>::Cast(" + from + ");\n");
      sb.append(sp + "std::vector<" + ((generic.isUnknownType(cppClass) || generic.name.startsWith(cppClass.name)) ? namespace.toString() : "") + generic.name + "> " + to + ";\n");
      sb.append(sp + "for (int i = 0; i < array->Length(); ++i) {\n");
      sb.append(sp + "  " + "v8::Local<v8::Value> tmp = array->Get(i);\n");
      sb.append(generic.unwrap("tmp", "x", sp + "  ", namespace, cppClass) + "\n");
      sb.append(sp + "  " + to + ".push_back(x);\n");
      sb.append(sp + "}");*/
      return sb.toString();
    } else if (name.endsWith("*")) {
      return sp + namespace + name + " " + to + "(node::ObjectWrap::Unwrap<" + name.replaceAll("(\\*|&)", "") + ">(" + from + "->ToObject())->getNwcpValue())" + (isReference ? "*" : "") + ";";
    } else {
      return sp + namespace + name.replaceAll("(\\*|&)", "") + " " + to + "(node::ObjectWrap::Unwrap<" + name.replaceAll("(\\*|&)", "") + ">(" + from + "->ToObject())->getNwcpValue())" + (isReference ? "*" : "") + ";";
    }
  }
}