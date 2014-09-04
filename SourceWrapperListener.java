import java.util.Iterator;
import java.util.List;
import java.util.LinkedList;

import org.antlr.v4.runtime.TokenStream;
import org.antlr.v4.runtime.misc.Interval;
import org.antlr.v4.runtime.misc.NotNull;

public class SourceWrapperListener extends nodewebkitwrapperBaseListener {
  nodewebkitwrapperParser parser;
  CppClass cppClass;
  CppNamespace cppNamespace = new CppNamespace();

  public SourceWrapperListener(nodewebkitwrapperParser p) {
    parser = p;
    cppClass = new CppClass();
  }

  private void p(String s) {
    System.out.println(s);
  }

  private String paramList(List<CppType> args) {
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < args.size(); ++i) {
      sb.append(args.get(i).unwrap("args[" + i + "]"));
      if (i + 1 < args.size()) {
        sb.append(", ");
      }
    }
    return sb.toString();
  }


  @Override public void enterNamespace(@NotNull nodewebkitwrapperParser.NamespaceContext ctx) {
    cppNamespace.push(ctx.Identifier().toString());
  }

  @Override public void exitNamespace(@NotNull nodewebkitwrapperParser.NamespaceContext ctx) {
    cppNamespace.pop();
  }

  @Override public void enterCppClass(@NotNull nodewebkitwrapperParser.CppClassContext ctx) {
    cppClass = new CppClass();
    cppClass.name = ctx.Identifier().toString();
  }

  @Override public void exitCppClass(@NotNull nodewebkitwrapperParser.CppClassContext ctx) {
    p("#include <iostream>");
    p("#include <node.h>");
    p("#include <nan.h>");
    p("#include \"" + cppClass.name + ".h\"");
    p("#include \"" + cppClass.name + "_wrap.h\"");
    p("");
    p("v8::Persistent<v8::Function> " + cppClass.name + "::constructor;");
    p("");
    p(cppClass.name + "::" + cppClass.name + "() : ObjectWrap(), " + cppClass.name.toLowerCase() + "(new " + cppNamespace + cppClass.name + "()) {};");
    p(cppClass.name + "::" + cppClass.name + "(" + cppNamespace + cppClass.name + "* o) : ObjectWrap(), " + cppClass.name.toLowerCase() + "(o) {};");
    p(cppClass.name + "::~" + cppClass.name + "() { delete " + cppClass.name.toLowerCase() + "; };");
    p("");
    p("NAN_METHOD(" + cppClass.name + "::New) {");
    p("  NanScope();");
    p("");
    p("  " + cppClass.name + "* obj = new " + cppClass.name + "();");
    p("  obj->Wrap(args.This());");
    p("");
    p("  NanReturnValue(args.This());");
    p("}");
    p("");
    p("NAN_METHOD(" + cppClass.name + "::NewInstance) {");
    p("  NanScope();");
    p("");
    p("  v8::Local<v8::Function> cons = NanNew<v8::Function>(constructor);");
    p("  v8::Local<v8::Object> instance = cons->NewInstance();");
    p("");
    p("  NanReturnValue(instance);");
    p("}");
    p("");
    p("void " + cppClass.name + "::Init(v8::Handle<v8::Object> exports) {");
    p("  NanScope();");
    p("");
    p("  // Prepare constructor template");
    p("  v8::Local<v8::FunctionTemplate> tpl = NanNew<v8::FunctionTemplate>(New);");
    p("  tpl->SetcppClass.name(NanNew<v8::String>(\"" + cppClass.name + "\"));");
    p("  tpl->InstanceTemplate()->SetInternalFieldCount(1);");
    p("");
    for (CppMethod m : cppClass.methods) {
      if (!m.isStatic) continue;
      p("  tpl->Set(NanNew<v8::String>(\"" + m.name + "\"),");
      p("      NanNew<v8::FunctionTemplate>(" + m.name + ")->GetFunction());");
    }
    p("");
    p("  // Prototype");
    for (CppMethod m : cppClass.methods) {
      if (m.isStatic) continue;
      p("  NanSetPrototypeTemplate(tpl, \"" + m.name + "\", NanNew<v8::String>(\"" + m.name + " prop?\"));");
    }
    p("");
    p("  NanAssignPersistent<v8::Function>(constructor, tpl->GetFunction());");
    p("  exports->Set(NanNew<v8::String>(\"" + cppClass.name + "\"), tpl->GetFunction());");
    p("}");
    p("");
    for (CppMethod m : cppClass.methods) {
      boolean isInstance = m.returnType.name.equals(cppClass.name + "*");
      boolean isClassType = !m.returnType.name.equals("int")
          || !m.returnType.name.startsWith("string")
          || !m.returnType.name.equals("bool")
          || !m.returnType.name.equals("time_t");
      if (m.isStatic && !isInstance) return;  // skip
      p("");
      p("NAN_METHOD(" + cppClass.name + "::" + m.name + ") {");
      p("  NanScope();");
      p("");
      if (!m.isStatic)
        p("  " + cppClass.name + "* obj = ObjectWrap::Unwrap<" + cppClass.name + ">(args.This());");
      if (isInstance) {
        p("  " + cppNamespace + cppClass.name + "* " + cppClass.name.toLowerCase() + " =");
        p("      " + cppNamespace + cppClass.name + "::" + m.name + "(" + paramList(m.args) + ");");
        p("  v8::Local<v8::Function> cons = NanNew<v8::Function>(constructor);");
        p("  v8::Local<v8::Object> instance = cons->NewInstance();");
        p("");
        p("  " + cppClass.name + "* obj = ObjectWrap::Unwrap<" + cppClass.name + ">(instance);");
        p("  obj->" + cppClass.name.toLowerCase() + " = " + cppClass.name.toLowerCase() + ";");
      } else {
        if (!m.returnType.isVoid)
          System.out.print("  " + (m.returnType.isConst ? "const " : "") + (isClassType ? cppNamespace : "") + m.returnType.name + " result =");
        p("  obj->" + cppClass.name.toLowerCase() + "->" + m.name + "(" + paramList(m.args) + ");");
      }
      p("");
      if (m.returnType.isVoid) {
        p("  NanReturnUndefined();");
      } else if (isInstance) {
        p("  NanReturnValue(instance);");
      } else if (isClassType) {
        String typeName = m.returnType.name.replaceAll("(&|\\*)", "");
        p("  " + m.returnType.name + " r = " + typeName + ".NewInstance();");
        p("  r->" + typeName.toLowerCase() + " = result;");
        p("  NanReturnValue(r);");
      } else {
        p("  NanReturnValue(" + m.returnType.wrap("result") + ");");
      }
      p("}");
    }
    p("");
  }

  @Override public void enterMethod(@NotNull nodewebkitwrapperParser.MethodContext ctx) {
    cppClass.methods.add(new CppMethod(ctx));
  }

}