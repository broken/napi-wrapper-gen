package com.dogatech.nodewebkitwrapper.prototype;

import java.util.ArrayList;
import java.util.List;

import com.dogatech.nodewebkitwrapper.grammar.nodewebkitwrapperParser;
import com.dogatech.nodewebkitwrapper.io.Outputter;
import com.dogatech.nodewebkitwrapper.prototype.type.CppType;
import com.dogatech.nodewebkitwrapper.prototype.type.CppTypeFactory;


public class CppMethod {
  public boolean isStatic;
  public boolean isConst;
  public String name;
  public CppClass cppClass;
  public CppType returnType;
  public List<CppType> args = new ArrayList<CppType>();
  public boolean isGetter; // TODO remove
  public boolean isSetter; // TODO remove
  private Outputter o;
  public boolean broken;

  public CppMethod(CppClass parentClass, nodewebkitwrapperParser.MethodContext ctx, Outputter out) {
    isStatic = ctx.STATIC() != null;
    isConst = ctx.CONST() != null;
    name = ctx.Identifier().toString();
    cppClass = parentClass;
    o = out;
    returnType = CppTypeFactory.instance().createType(ctx.type(), cppClass, o);
    for (nodewebkitwrapperParser.ParameterContext p : ctx.parameterList().parameter()) {
      args.add(CppTypeFactory.instance().createType(p.type(), cppClass, o));
    }

    for (MethodType mt : types) {
      if (mt.canHandle(ctx)) {
        type = mt;
        break;
      }
    }
    for (MethodAccess ma : accesses) {
      if (ma.canHandle(ctx)) {
        access = ma;
        break;
      }
    }
    // TODO remove
    isGetter = type instanceof MtGetter;
    isSetter = type instanceof MtSetter;

    broken = returnType == null || type == null || access == null || args.contains(null);
  }

  public boolean isInstanceOf(CppClass cppClass) {
    return returnType.name.equals(cppClass.name + "*");
  }

  public String accessor() {
    return name.substring(3,4).toLowerCase() + name.substring(4);
  }

  public void outputHeader() {
    if (broken) {
      o.i().p("// Unable to process " + name);
      return;
    }
    if (isGetter) {
      o.i().p("static void " + name + "(v8::Local<v8::String> property, const Nan::PropertyCallbackInfo<v8::Value>& info);");
    } else if (isSetter) {
      o.i().p("static void " + name + "(v8::Local<v8::String> property, v8::Local<v8::Value> value, const Nan::PropertyCallbackInfo<void>& info);");
    } else {
      o.i().p("static void " + name + "(const Nan::FunctionCallbackInfo<v8::Value>& info);");
    }
  }

  public void outputSource(String namespace, CppClass cppClass) {
    if (broken) return;
    type.out();
    if (!isStatic)
      o.i().p(cppClass.name + "* obj = Nan::ObjectWrap::Unwrap<" + cppClass.name + ">(info.Holder());");
    for (int i = 0; i < args.size(); ++i) {
      String arg = type instanceof MtSetter ? "value" : "info[" + i + "]";
      args.get(i).outputUnwrap(arg, "a" + i);
    }
    returnType.outputResult();
    access.out();
    o.p("");
    returnType.outputReturn();
    o.decIndent().i().p("}");
    o.p("");
  }

  public void outputDeclaration() {
    if (broken) {
      o.i().p("// Unable to process " + name);
      return;
    }
    if (isStatic) {
      o.i().p("Nan::SetMethod(tpl, \"" + name + "\", " + name + ");");
    } else if (!isGetter && !isSetter) {
      o.i().p("Nan::SetPrototypeMethod(tpl, \"" + name + "\", " + name + ");");
    } else {
      if (isGetter) {
        o.i().p("Nan::SetAccessor(tpl->InstanceTemplate(), Nan::New<v8::String>(\"" + accessor() + "\").ToLocalChecked(), " + name, false);
        if (cppClass.setters.contains(accessor())) {  // TODO check if setter is broken
          o.p(", " + "s" + name.substring(1) + ");");
        } else {
          o.p(");");
        }
      }
    }
  }

  private interface MethodPart {
    public void out();
    public boolean canHandle(nodewebkitwrapperParser.MethodContext ctx);
  }

  private MethodType type;
  private MethodType[] types = { new MtGetter(), new MtSetter(), new MtGeneric() };
  private abstract class MethodType implements MethodPart {
    public abstract String nanMethod();
    @Override
    public void out() {
      if (isGetter || isSetter) {
        o.p(nanMethod() + "(" + cppClass.name + "::" + name + ") {").incIndent();
      } else {
        o.p("void " + cppClass.name + "::" + name + "(const Nan::FunctionCallbackInfo<v8::Value>& info) {").incIndent();
      }
    }
  }
  private class MtGetter extends MethodType {
    @Override
    public String nanMethod() { return "NAN_GETTER"; }
    @Override
    public boolean canHandle(nodewebkitwrapperParser.MethodContext ctx) {
      return name.startsWith("get")
          && name.substring(3,4).equals(name.substring(3,4).toUpperCase())
          && args.size() == 0;
    }
  }
  private class MtSetter extends MethodType {
    @Override
    public String nanMethod() { return "NAN_SETTER"; }
    @Override
    public boolean canHandle(nodewebkitwrapperParser.MethodContext ctx) {
      return name.startsWith("set")
          && name.substring(3,4).equals(name.substring(3,4).toUpperCase())
          && args.size() == 1;
    }
  }
  private class MtGeneric extends MethodType {
    @Override
    public String nanMethod() { return "NAN_METHOD"; }
    @Override
    public boolean canHandle(nodewebkitwrapperParser.MethodContext ctx) { return true; }
  }

  private MethodAccess access;
  private MethodAccess[] accesses = {
    new MaStatic(),
    new MaGeneric()
  };
  private abstract class MethodAccess implements MethodPart {
    protected String paramList(List<CppType> args) {
      StringBuilder sb = new StringBuilder();
      for (int i = 0; i < args.size(); ++i) {
        sb.append("a" + i);
        if (i + 1 < args.size()) {
          sb.append(", ");
        }
      }
      return sb.toString();
    }
  }
  private class MaInstance extends MethodAccess {
    @Override
    public boolean canHandle(nodewebkitwrapperParser.MethodContext ctx) {
      return isInstanceOf(cppClass);
    }
    @Override
    public void out() {
      o.p("");
      o.i().p("    " + cppClass.namespace + cppClass.name + "::" + name + "(" + paramList(args) + ");");
      o.i().p("v8::Local<v8::Function> cons = Nan::New<v8::Function>(constructor);");
      o.i().p("v8::Local<v8::Object> instance = Nan::NewInstance(cons).ToLocalChecked();");
      o.p("");
      o.i().p(cppClass.name + "* obj = Nan::ObjectWrap::Unwrap<" + cppClass.name + ">(instance);");
      o.i().p("obj->" + cppClass.name.toLowerCase() + " = result;");
    }
  }
  private class MaStatic extends MethodAccess {
    @Override
    public boolean canHandle(nodewebkitwrapperParser.MethodContext ctx) {
      return isStatic;
    }
    @Override
    public void out() {
      o.p("");
      o.i().p("    dogatech::soulsifter::" + cppClass.name + "::" + name + "(" + paramList(args) + ");");
    }
  }
  private class MaGeneric extends MethodAccess {
    @Override
    public boolean canHandle(nodewebkitwrapperParser.MethodContext ctx) {
      return true;
    }
    @Override
    public void out() {
      o.i().p("obj->" + cppClass.name.toLowerCase() + "->" + name + "(" + paramList(args) + ");");
    }
  }
}