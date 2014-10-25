package com.dogatech.nodewebkitwrapper.prototype;

import com.dogatech.nodewebkitwrapper.grammar.nodewebkitwrapperParser;


public class CppType {
  public boolean isVoid;
  public boolean isSet;
  public boolean isVector;

  public boolean isConst = false;
  public boolean isPointer = false;
  public boolean isPointerToPointer = false;
  public boolean isReference = false;
  public String name;

  public CppType(nodewebkitwrapperParser.TypeContext ctx) {
    isConst = ctx.CONST() != null;
    name = ctx.Identifier().toString();
    isVoid = name.equals("void");
    isSet = name.startsWith("set<");
    isVector = name.startsWith("vector<");
    isPointer = name.charAt(name.length() - 1) == '*';
    isReference = name.charAt(name.length() - 1) == '&';
  }

  public CppType(String type) {
    name = type;
    isConst = name.startsWith("const");
    isVoid = name.equals("void");
    isSet = name.startsWith("set<");
    isVector = name.startsWith("vector<");
    isPointer = name.charAt(name.length() - 1) == '*';
    isReference = name.charAt(name.length() - 1) == '&';
  }

  public boolean isUnknownType(CppClass c) {
    return (!name.equals("int") &&
           !name.startsWith("string") &&
           !name.equals("bool") &&
           !name.equals("time_t") &&
           !name.equals("void") &&
           !name.startsWith("vector<") &&
           !name.equals("ResultSetIterator<" + c.name + ">*") &&
           !name.startsWith(c.name)) ||
           name.endsWith("**");
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
      CppType generic = new CppType(getGeneric());
      sb.append(sp + "v8::Local<v8::Array> array = v8::Local<v8::Array>::Cast(" + from + ");\n");
      sb.append(sp + "std::vector<" + ((generic.isUnknownType(cppClass) || generic.name.startsWith(cppClass.name)) ? namespace.toString() : "") + generic.name + "> " + to + ";\n");
      sb.append(sp + "for (int i = 0; i < array->Length(); ++i) {\n");
      sb.append(sp + "  " + "v8::Local<v8::Value> tmp = array->Get(i);\n");
      sb.append(generic.unwrap("tmp", "x", sp + "  ", namespace, cppClass) + "\n");
      sb.append(sp + "  " + to + ".push_back(x);\n");
      sb.append(sp + "}");
      return sb.toString();
    } else if (name.endsWith("*")) {
      return sp + namespace + name + " " + to + "(node::ObjectWrap::Unwrap<" + name.replaceAll("(\\*|&)", "") + ">(" + from + "->ToObject())->getNwcpValue())" + (isReference ? "*" : "") + ";";
    } else {
      return sp + namespace + name.replaceAll("(\\*|&)", "") + " " + to + "(node::ObjectWrap::Unwrap<" + name.replaceAll("(\\*|&)", "") + ">(" + from + "->ToObject())->getNwcpValue())" + (isReference ? "*" : "") + ";";
    }
  }

  public String wrap(String var) {
    if (name.equals("int")) {
      return "NanNew<v8::Number>(" + var + ")";
    } else if (name.startsWith("string")) {
      return "NanNew<v8::String>(" + var + ".c_str(), " + var + ".length())";
    } else if (name.equals("bool")) {
      return "NanNew<v8::Boolean>(" + var + ")";
    } else if (name.equals("time_t")) {
      return "NanNew<v8::Number>(" + var + "* 1000)";
    } else {
      return "";
    }
  }
}