package com.dogatech.napiwrapper.prototype;

import java.util.ArrayList;
import java.util.List;

import com.dogatech.napiwrapper.grammar.napiwrapperParser;
import com.dogatech.napiwrapper.io.Outputter;
import com.dogatech.napiwrapper.prototype.type.CppType;
import com.dogatech.napiwrapper.prototype.type.CppTypeFactory;
import com.dogatech.napiwrapper.prototype.type.FunctionType;
import com.dogatech.napiwrapper.prototype.type.FutureType;
import com.dogatech.napiwrapper.prototype.type.SoulSifterModelType;
import com.dogatech.napiwrapper.prototype.type.VoidType;


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
  public boolean isAsync;
  public boolean hasProgressCallback;
  public int minNumArgs;

  public CppMethod(CppClass parentClass, napiwrapperParser.MethodContext ctx, Outputter out) {
    isStatic = ctx.STATIC() != null;
    isConst = ctx.CONST() != null;
    name = ctx.Identifier().toString();
    cppClass = parentClass;
    o = out;
    minNumArgs = 0;
    isAsync = false;
    hasProgressCallback = false;
    returnType = CppTypeFactory.instance().createType(ctx.type(), cppClass, o);
    if (returnType instanceof FutureType) isAsync = true;
    for (napiwrapperParser.ParameterContext p : ctx.parameterList().parameter()) {
      CppType t = CppTypeFactory.instance().createType(p.type(0), cppClass, o);
      args.add(t);
      if (isProgressCallback(t)) isAsync = hasProgressCallback = true;
      if (t instanceof FunctionType && p.Identifier().toString().equals("alertCallback")) ((FunctionType) t).isAlertCallback = true;
      if (p.EQUALS() == null) minNumArgs += 1;
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

  public String accessor() {
    return name.substring(3,4).toLowerCase() + name.substring(4);
  }

  public void outputHeader() {
    if (broken) {
      o.i().p("// Unable to process " + name);
      return;
    }
    if (isSetter) {
      o.i().p("void " + name + "(const Napi::CallbackInfo& info, const Napi::Value &value);");
    } else {
      o.i().p((isStatic ? "static " : "") + (returnType instanceof VoidType ? "void " : "Napi::Value ") + name + "(const Napi::CallbackInfo& info);");
    }
  }

  public void outputSource(String namespace, CppClass cppClass) {
    if (broken) return;
    if (isAsync)
      if (!hasProgressCallback) outputSourceAsyncClass(namespace, cppClass);
      else outputSourceAsyncProgressClass(namespace, cppClass);
    type.out();
    if (!(returnType instanceof VoidType) || args.size() > 0)
      o.i().p("Napi::Env env = info.Env();");
    if (type instanceof MtPromise) o.i().p("std::shared_ptr<Napi::Promise::Deferred> deferred = std::make_shared<Napi::Promise::Deferred>(Napi::Promise::Deferred::New(env));");
    if (args.size() > 0) {
      o.i().p("if (info.Length() < " + args.size() + ") {").incIndent();  // TODO: this should be minNumArgs
      type.errOut("Expected at least " + minNumArgs + " argument" + (minNumArgs > 1 ? "s" : "") + ".");
      o.decIndent().i().p("}");
    }
    if (!isStatic)
      o.i().p(cppClass.name + "* obj = this;");
    for (int i = 0; i < args.size(); ++i) {
      String arg = type instanceof MtSetter ? "value" : "info[" + i + "]";
      args.get(i).outputUnwrap(arg, "a" + i, type);
    }
    returnType.outputResult();
    access.out();
    if (!(returnType instanceof VoidType) && !(returnType instanceof FutureType)) o.p("");
    returnType.outputReturn();
    o.decIndent().i().p("}");
    o.p("");
  }

  private String workerName() {
    return name.substring(0, 1).toUpperCase() + name.substring(1) + "Worker";
  }

  private void outputSourceAsyncClass(String namespace, CppClass cppClass) {
    o.p("class " + workerName() + " : public Napi::AsyncWorker {");
    o.p(" public:").incIndent();
    o.i().p(workerName() + "(Napi::Env env, std::shared_ptr<Napi::Promise::Deferred> d", false);
    StringBuilder sb = new StringBuilder();
    int num = 0;
    for (int i = 0; i < args.size(); ++i) {
      sb.append(", ");
      sb.append(args.get(i).fullName());
      sb.append(" a" + i);
    }
    sb.append(")");
    o.p(sb.toString());
    o.i().p("    : Napi::AsyncWorker(env), deferred(d)", false);
    for (int i = 0; i < args.size(); ++i) {
      if (args.get(i) instanceof SoulSifterModelType && args.get(i).isPointer()) {
        o.p(", a" + i + "x(*a" + i + "), a" + i + "(&a" + i + "x)", false);
      } else {
        o.p(", a" + i + "(a" + i + ")", false);
      }
    }
    o.p(" {").incIndent();
    o.decIndent().i().p("}");
    o.p("");
    o.i().p("~" + workerName() + "() { }");
    o.p("");
    o.i().p("void Execute() {").incIndent();
    o.i().p(returnType.fullName() + " result =");
    o.i().p("    dogatech::soulsifter::" + cppClass.name + "::" + name + "(" + access.paramList(args) + ");");
    o.i().p("res = result.get();");
    o.decIndent().i().p("}");
    o.p("");
    o.i().p("void OnOK() {").incIndent();
    o.i().p("Napi::Env env = Env();");
    o.i().p("Napi::HandleScope scope(env);");
    returnType.generics.get(0).outputWrap("res", "wrapped_result");
    o.i().p("deferred->Resolve(wrapped_result);");
    o.decIndent().i().p("}");
    o.p("");
    o.i().p("void OnError(const Napi::Error& e) {").incIndent();
    o.i().p("Napi::Env env = Env();");
    o.i().p("Napi::HandleScope scope(env);");
    o.i().p("deferred->Reject(Napi::TypeError::New(env, \"Failed to process async function " + name + "\").Value());");
    o.decIndent().i().p("}");
    o.p("");
    o.decIndent().i().p(" private:").incIndent();
    o.i().p("std::shared_ptr<Napi::Promise::Deferred> deferred;");
    o.i().p(returnType.generics.get(0).fullName() + " res;");
    for (int i = 0; i < args.size(); ++i) {
      if (args.get(i) instanceof SoulSifterModelType && args.get(i).isPointer()) {
        o.i().p(args.get(i).fullName(true) + " a" + i + "x;");
        o.i().p(args.get(i).fullName() + " a" + i + ";");
      } else {
        o.i().p(args.get(i).fullName(true) + " a" + i + ";");
      }
    }
    o.decIndent().i().p("};");
    o.p("");
  }

  private void outputSourceAsyncProgressClass(String namespace, CppClass cppClass) {
    o.p("class " + workerName() + " : public Napi::AsyncProgressWorker<float> {");
    o.p(" public:").incIndent();
    o.i().p(workerName() + "(", false);
    StringBuilder sb = new StringBuilder();
    int num = 0;
    for (int i = 0; i < args.size(); ++i) {
      if (i > 0) sb.append(", ");
      if (isProgressCallback(args.get(i))) {
        sb.append("Napi::Function&");
        num = i;
      } else {
        sb.append(args.get(i).name);
      }
      sb.append(" a" + i);
    }
    sb.append(")");
    o.p(sb.toString());
    o.i().p("    : Napi::AsyncProgressWorker<float>(a0) {").incIndent();
    for (int i = 0; i < args.size(); ++i) {
      if (!isProgressCallback(args.get(i))) {
        String arg = type instanceof MtSetter ? "value" : "info[" + i + "]";
        args.get(i).outputUnwrap(arg, "a" + i, type);
        o.i().p("arg" + i + " = a" + i + ";");
      }
    }
    o.decIndent().i().p("}");
    o.p("");
    o.i().p("~" + workerName() + "() { }");
    o.p("");
    o.i().p("void Execute(const Napi::AsyncProgressWorker<float>::ExecutionProgress& ep) {").incIndent();
    for (int i = 0; i < args.size(); ++i) {
      if (isProgressCallback(args.get(i))) {
        o.i().p("auto a" + i, false);
      }
    }
    o.p(" = [&ep](float p) {").incIndent();
    o.i().p("ep.Send(&p, 1);");
    o.decIndent().i().p("};");
    o.i().p("dogatech::soulsifter::" + cppClass.name + "::" + name + "(" + access.paramList(args) + ");");
    o.decIndent().i().p("}");
    o.p("");
    o.i().p("void OnProgress(const float *data, size_t count) {").incIndent();
    o.i().p("Napi::HandleScope scope(Env());");
    o.i().p("Callback().Call({Env().Null(), Env().Null(), Napi::Number::New(Env(), *data)});");
    o.decIndent().i().p("}");
    o.p("");
    o.i().p("void OnOK() {").incIndent();
    o.i().p("Napi::HandleScope scope(Env());");
    o.i().p("Callback().Call({Env().Null(), Napi::String::New(Env(), \"done\"), Env().Null()});");
    o.decIndent().i().p("}");
    o.p("");
    o.decIndent().i().p(" private:").incIndent();
    for (int i = 0; i < args.size(); ++i) {
      if (!isProgressCallback(args.get(i))) {
        CppType t = args.get(i);
        o.i().p(t.name + " arg" + i + ";");
      }
    }
    o.decIndent().i().p("};");
    o.p("");
  }

  public void outputDeclaration() {
    if (broken) {
      o.i().p("// Unable to process " + name);
      return;
    }
    if (isStatic) {
      o.i().p("StaticMethod<&" + cppClass.name + "::" + name + ">(\"" + name + "\"),");
    } else if (!isGetter && !isSetter) {
      o.i().p("InstanceMethod<&" + cppClass.name + "::" + name + ">(\"" + name + "\"),");
    } else {
      if (isGetter) {
        o.i().p("InstanceAccessor<&" + cppClass.name + "::" + name, false);
        if (cppClass.setters.contains(accessor())) {  // TODO check if setter is broken
          o.p(", &" + cppClass.name + "::s" + name.substring(1), false);
        }
        o.p(">(\"" + accessor() + "\"),");
      }
    }
  }

  private boolean isProgressCallback(CppType type) {
    return type instanceof FunctionType && type.generics.size() == 1 && type.generics.get(0).name.equals("float");
  }

  private boolean isCompleteCallback(CppType type) {
    return type instanceof FunctionType && type.generics.size() == 1 && type.generics.get(0).name.endsWith("string");
  }

  private interface MethodPart {
    public void out();
    public boolean canHandle(napiwrapperParser.MethodContext ctx);
  }

  private MethodType type;
  private MethodType[] types = { new MtPromise(), new MtGetter(), new MtSetter(), new MtGeneric() };
  public abstract class MethodType implements MethodPart {
    @Override
    public void out() {
      if (isSetter) {
        o.i().p("void " + cppClass.name + "::" + name + "(const Napi::CallbackInfo& info, const Napi::Value &value) {").incIndent();
      } else {
        o.i().p((returnType instanceof VoidType ? "void " : "Napi::Value ") + cppClass.name + "::" + name + "(const Napi::CallbackInfo& info) {").incIndent();
      }
    }
    public void errOut(String msg) {
      o.i().p("Napi::TypeError::New(env, \"" + msg + "\").ThrowAsJavaScriptException();");
      o.i().p("return", false);
      if (!(returnType instanceof VoidType)) o.p(" env.Null()", false);
      o.p(";");
    }
  }
  private class MtGetter extends MethodType {
    @Override
    public boolean canHandle(napiwrapperParser.MethodContext ctx) {
      return name.length() > 3
          && name.startsWith("get")
          && name.substring(3,4).equals(name.substring(3,4).toUpperCase())
          && args.size() == 0;
    }
  }
  private class MtSetter extends MethodType {
    @Override
    public boolean canHandle(napiwrapperParser.MethodContext ctx) {
      return name.length() > 3
          && name.startsWith("set")
          && name.substring(3,4).equals(name.substring(3,4).toUpperCase())
          && args.size() == 1;
    }
  }
  private class MtGeneric extends MethodType {
    @Override
    public boolean canHandle(napiwrapperParser.MethodContext ctx) { return true; }
  }
  private class MtPromise extends MethodType {
    @Override
    public boolean canHandle(napiwrapperParser.MethodContext ctx) {
      return returnType instanceof FutureType;
    }
    @Override
    public void errOut(String msg) {
      o.i().p("deferred->Reject(Napi::String::New(env, \"" + msg + "\"));");
      o.i().p("return deferred->Promise();");
    }
  }

  private MethodAccess access;
  private MethodAccess[] accesses = {
    new MaAsync(),
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
    public boolean canHandle(napiwrapperParser.MethodContext ctx) {
      return returnType.name.equals(cppClass.name + "*");
    }
    @Override
    public void out() {
      o.p("");
      o.i().p("    " + cppClass.namespace + cppClass.name + "::" + name + "(" + paramList(args) + ");");
      o.i().p("v8::Local<v8::Function> cons = Nan::New<v8::Function>(constructor);");
      o.i().p("v8::Local<v8::Object> instance = Nan::NewInstance(cons).ToLocalChecked();");
      o.p("");
      o.i().p(cppClass.name + "* obj = Napi::ObjectWrap<" + cppClass.name + ">::Unwrap(instance);");
      o.i().p("obj->" + cppClass.name.toLowerCase() + " = result;");
    }
  }
  private class MaStatic extends MethodAccess {
    @Override
    public boolean canHandle(napiwrapperParser.MethodContext ctx) {
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
    public boolean canHandle(napiwrapperParser.MethodContext ctx) {
      return true;
    }
    @Override
    public void out() {
      o.i().p("obj->" + cppClass.name.toLowerCase() + "->" + name + "(" + paramList(args) + ");");
    }
  }
  private class MaAsync extends MethodAccess {
    @Override
    public boolean canHandle(napiwrapperParser.MethodContext ctx) {
      return isAsync;
    }
    @Override
    public void out() {
      o.i().p(workerName() + "* w = new " + workerName() + "(", false);
      if (!hasProgressCallback) o.p("env, deferred, ", false);
      o.p(paramList(args) + ");");
      o.i().p("w->Queue();");
      o.i().p("return", false);
      if (returnType instanceof FutureType) o.p(" deferred->Promise()", false);
      else if (!(returnType instanceof VoidType)) o.p(" env.Null()", false);
      o.p(";");
    }
  }
}
