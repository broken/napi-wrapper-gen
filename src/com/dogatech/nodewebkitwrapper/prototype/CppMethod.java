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
  private boolean broken;

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
    o.i().p("static " + type.nanMethod() + "(" + name + ");");
  }

  public void outputSource(String namespace, CppClass cppClass) {
    if (broken) return;
    type.out();
    o.i().p("NanScope();");
    o.p("");
    if (!isStatic)
      o.i().p(cppClass.name + "* obj = ObjectWrap::Unwrap<" + cppClass.name + ">(args.This());");
    for (int i = 0; i < args.size(); ++i) {
      String arg = type instanceof MtSetter ? "value" : "args[" + i + "]";
      o.p(args.get(i).unwrap(arg, "a" + i, "  ", namespace, cppClass));
    }
    returnType.outputResult();
    access.out();
    o.p("");
    returnType.outputReturn();
    o.decIndent().i().p("}");
  }

  public void outputDeclaration() {
    if (broken) {
      o.i().p("// Unable to process " + name);
      return;
    }
    if (isStatic) {
      o.i().p("NanSetTemplate(tpl, \"" + name + "\", NanNew<v8::FunctionTemplate>(" + name + ")->GetFunction());");
    } else if (!isGetter && !isSetter) {
      o.i().p("NanSetPrototypeTemplate(tpl, \"" + name + "\", NanNew<v8::FunctionTemplate>(" + name + ")->GetFunction());");
    } else {
      if (isGetter) {
        o.i().p("tpl->InstanceTemplate()->SetAccessor(NanNew<v8::String>(\"" + accessor() + "\"), " + name, false);
        if (true) {// TODO cppClass.setters.contains(accessor())) {
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
      o.p(nanMethod() + "(" + cppClass.name + "::" + name + ") {").incIndent();
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
      //o.i().p(cppClass.namespace + cppClass.name + "* " + cppClass.name.toLowerCase() + " =");
      o.p("");
      o.i().p("  " + cppClass.namespace + cppClass.name + "::" + name + "(" + paramList(args) + ");");
      o.i().p("v8::Local<v8::Function> cons = NanNew<v8::Function>(constructor);");
      o.i().p("v8::Local<v8::Object> instance = cons->NewInstance();");
      o.p("");
      o.i().p(cppClass.name + "* obj = ObjectWrap::Unwrap<" + cppClass.name + ">(instance);");
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
      /*if (returnType.isUnknownType(cppClass)) {
        o.i().p("if (result == NULL) NanReturnUndefined();");
      }*/
    }
  }
}