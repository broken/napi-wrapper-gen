import java.io.IOException;
import java.io.OutputStream;
import java.util.Iterator;
import java.util.List;
import java.util.LinkedList;

import org.antlr.v4.runtime.TokenStream;
import org.antlr.v4.runtime.misc.Interval;
import org.antlr.v4.runtime.misc.NotNull;

public class SourceWrapperListener extends nodewebkitwrapperBaseListener {
  nodewebkitwrapperParser parser;
  CppClass cppClass = new CppClass();
  CppNamespace cppNamespace = new CppNamespace();
  OutputStream os;

  public SourceWrapperListener(nodewebkitwrapperParser p) {
    parser = p;
    os = System.out;
  }

  public SourceWrapperListener(nodewebkitwrapperParser p, OutputStream out) {
    parser = p;
    os = out;
  }

  private void p(String s, boolean nl) {
    try {
      os.write(s.getBytes());
      if (nl) os.write("\n".getBytes());
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  private void p(String s) {
    p(s, true);
  }

  private String paramList(List<CppType> args) {
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < args.size(); ++i) {
      sb.append("a" + i);
      if (i + 1 < args.size()) {
        sb.append(", ");
      }
    }
    return sb.toString();
  }

  private boolean isSingleton() {
    for (CppMethod m : cppClass.methods) {
      if (m.name.equals("getInstance")) {
        return true;
      }
    }
    return false;
  }

  public boolean skipMethod(CppMethod m) {
    if (m.returnType.isUnknownType(cppClass)) return true;
    boolean cannotHandleArg = false;
    for (CppType t : m.args) {
      cannotHandleArg |= t.isUnknownType(cppClass);
    }
    if (cannotHandleArg) return true;
    return false;
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
    if (isSingleton()) {
      p(cppClass.name + "::" + cppClass.name + "() : ObjectWrap(), " + cppClass.name.toLowerCase() + "(&(" + cppNamespace + cppClass.name + "::getInstance())) {};");
    } else {
      p(cppClass.name + "::" + cppClass.name + "() : ObjectWrap(), " + cppClass.name.toLowerCase() + "(new " + cppNamespace + cppClass.name + "()) {};");
    }
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
    p("  tpl->SetClassName(NanNew<v8::String>(\"" + cppClass.name + "\"));");
    p("  tpl->InstanceTemplate()->SetInternalFieldCount(1);");
    p("");
    for (CppMethod m : cppClass.methods) {
      if (skipMethod(m) || !m.isStatic) continue;
      p("  NanSetTemplate(tpl, \"" + m.name + "\", NanNew<v8::FunctionTemplate>(" + m.name + ")->GetFunction());");
    }
    p("");
    p("  // Prototype");
    for (CppMethod m : cppClass.methods) {
      if (skipMethod(m) || m.isStatic) continue;
      p("  NanSetPrototypeTemplate(tpl, \"" + m.name + "\", NanNew<v8::FunctionTemplate>(" + m.name + ")->GetFunction());");
    }
    p("");
    p("  NanAssignPersistent<v8::Function>(constructor, tpl->GetFunction());");
    p("  exports->Set(NanNew<v8::String>(\"" + cppClass.name + "\"), tpl->GetFunction());");
    p("}");
    p("");
    for (CppMethod m : cppClass.methods) {
      if (skipMethod(m)) continue;
      p("");
      p("NAN_METHOD(" + cppClass.name + "::" + m.name + ") {");
      p("  NanScope();");
      p("");
      if (!m.isStatic)
        p("  " + cppClass.name + "* obj = ObjectWrap::Unwrap<" + cppClass.name + ">(args.This());");
      for (int i = 0; i < m.args.size(); ++i) {
        p(m.args.get(i).unwrap("args[" + i + "]", "a" + i, "  ", cppNamespace.toString(), cppClass));
      }
      if (m.isInstanceOf(cppClass)) {
        p("  " + cppNamespace + cppClass.name + "* " + cppClass.name.toLowerCase() + " =");
        p("      " + cppNamespace + cppClass.name + "::" + m.name + "(" + paramList(m.args) + ");");
        p("  v8::Local<v8::Function> cons = NanNew<v8::Function>(constructor);");
        p("  v8::Local<v8::Object> instance = cons->NewInstance();");
        p("");
        p("  " + cppClass.name + "* obj = ObjectWrap::Unwrap<" + cppClass.name + ">(instance);");
        p("  obj->" + cppClass.name.toLowerCase() + " = " + cppClass.name.toLowerCase() + ";");
      } else if (m.returnType.name.equals("ResultSetIterator<" + cppClass.name + ">*")) {
        p("  dogatech::ResultSetIterator<" + cppNamespace + cppClass.name + ">* result =");
        p("      " + cppNamespace + cppClass.name + "::" + m.name + "(" + paramList(m.args) + ");");
      } else if (m.returnType.name.equals("vector<" + cppClass.name + "*>&") ||
            m.returnType.name.equals("vector<" + cppClass.name + "*>")) {
        p("  " + (m.returnType.isConst ? "const " : "") + "vector<" + cppNamespace + cppClass.name + "*> result =", false);
        p("  obj->" + cppClass.name.toLowerCase() + "->" + m.name + "(" + paramList(m.args) + ");");
      } else {
        if (!m.returnType.isVoid)
          p("  " + (m.returnType.isConst ? "const " : "") + (m.isClassType ? cppNamespace : "") + (m.returnType.isReference ? m.returnType.name.substring(0, m.returnType.name.length()-1) : m.returnType.name) + " result =", false);
        p("  obj->" + cppClass.name.toLowerCase() + "->" + m.name + "(" + paramList(m.args) + ");");
      }
      p("");
      if (m.returnType.isVoid) {
        p("  NanReturnUndefined();");
      } else if (m.isInstanceOf(cppClass)) {
        p("  NanReturnValue(instance);");
      } else if (m.isClassType) {
        String typeName = m.returnType.name.replaceAll("(&|\\*)", "");
        p("  " + m.returnType.name + " r = " + typeName + ".NewInstance();");
        p("  r->" + typeName.toLowerCase() + " = result;");
        p("  NanReturnValue(r);");
      } else if (m.returnType.name.equals("vector<" + cppClass.name + "*>&") ||
            m.returnType.name.equals("vector<" + cppClass.name + "*>")) {
        p("  v8::Handle<v8::Array> a = NanNew<v8::Array>((int) result.size());");
        p("  for (int i = 0; i < (int) result.size(); i++) {");
        p("    v8::Local<v8::Function> cons = NanNew<v8::Function>(constructor);");
        p("    v8::Local<v8::Object> instance = cons->NewInstance();");
        p("    " + cppClass.name + "* o = ObjectWrap::Unwrap<" + cppClass.name + ">(instance);");
        p("    o->" + cppClass.name.toLowerCase() + " = result[i];");
        p("    a->Set(NanNew<v8::Number>(i), instance);");
        p("  }");
        p("  NanReturnValue(a);");
      } else if (m.returnType.name.equals("ResultSetIterator<" + cppClass.name + ">*")) {
        p("  vector<" + cppNamespace + cppClass.name + "*>* v = result->toVector();");
        p("  v8::Handle<v8::Array> a = NanNew<v8::Array>((int) v->size());");
        p("  for (int i = 0; i < (int) v->size(); i++) {");
        p("    v8::Local<v8::Function> cons = NanNew<v8::Function>(constructor);");
        p("    v8::Local<v8::Object> instance = cons->NewInstance();");
        p("    " + cppClass.name + "* o = ObjectWrap::Unwrap<" + cppClass.name + ">(instance);");
        p("    o->" + cppClass.name.toLowerCase() + " = (*v)[i];");
        p("    a->Set(NanNew<v8::Number>(i), instance);");
        p("  }");
        p("  delete v;");
        p("  NanReturnValue(a);");
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