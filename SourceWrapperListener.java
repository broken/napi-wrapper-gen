import java.util.Iterator;
import java.util.List;
import java.util.LinkedList;
import java.util.Stack;

import org.antlr.v4.runtime.TokenStream;
import org.antlr.v4.runtime.misc.Interval;
import org.antlr.v4.runtime.misc.NotNull;

public class SourceWrapperListener extends nodewebkitwrapperBaseListener {
  nodewebkitwrapperParser parser;
  String className;
  String ns;
  Stack<String> namespaces;
  List<String> methods;
  List<String> staticMethods;

  public SourceWrapperListener(nodewebkitwrapperParser p) {
    parser = p;
    ns = "";
    namespaces = new Stack<String>();
    methods = new LinkedList<String>();
    staticMethods = new LinkedList<String>();
  }

  private void p(String s) {
    System.out.println(s);
  }

  private void setNamespace() {
    Iterator<String> it = namespaces.iterator();
    StringBuilder sb = new StringBuilder();
    while (it.hasNext()) {
        sb.append(it.next()).append("::");
    }
    ns = sb.toString();
  }

  private String wrap(nodewebkitwrapperParser.TypeContext type, String name) {
    if (type.typeName().getText().equals("int")) {
      return "NanNew<v8::Number>(" + name + ")";
    } else if (type.typeName().getText().startsWith("string")) {
      return "NanNew<v8::String>(" + name + ".c_str(), " + name + ".length())";
    } else if (type.typeName().getText().equals("bool")) {
      return "NanNew<v8::Boolean>(" + name + ")";
    } else if (type.typeName().getText().equals("time_t")) {
      return "NanNew<v8::Number>(" + name + "* 1000)";
    } else {
      return "";
    }
  }

  private String unwrap(nodewebkitwrapperParser.TypeContext type, String name) {
    if (type.typeName().getText().equals("int")) {
      return name + "->Uint32Value()";
    } else if (type.typeName().getText().startsWith("string&")) {
      return "*v8::String::Utf8Value(" + name + "->ToString())";
    } else if (type.typeName().getText().equals("bool")) {
      return name + "->BooleanValue()";
    } else if (type.typeName().getText().equals("time_t")) {
      return name + "->Uint32Value() / 1000";
    } else {
      return type.typeName().getText();
    }
  }

  private String paramList(nodewebkitwrapperParser.ParameterListContext ctx) {
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < ctx.parameter().size(); ++i) {
      sb.append(unwrap(ctx.parameter().get(i).type(), "args[" + i + "]"));
      if (i + 1 < ctx.parameter().size()) {
        sb.append(", ");
      }
    }
    return sb.toString();
  }


  @Override public void enterNamespace(@NotNull nodewebkitwrapperParser.NamespaceContext ctx) {
    namespaces.push(ctx.Identifier().toString());
    setNamespace();
  }

  @Override public void exitNamespace(@NotNull nodewebkitwrapperParser.NamespaceContext ctx) {
    namespaces.pop();
    setNamespace();
  }

  @Override public void enterCppClass(@NotNull nodewebkitwrapperParser.CppClassContext ctx) {
    className = ctx.Identifier().toString();
    p("#include <iostream>");
    p("#include <node.h>");
    p("#include <nan.h>");
    p("#include \"" + className + ".h\"");
    p("#include \"" + className + "_wrap.h\"");
    p("");
    p("v8::Persistent<v8::Function> " + className + "::constructor;");
    p("");
    p(className + "::" + className + "() : ObjectWrap(), " + className.toLowerCase() + "(new " + ns + className + "()) {};");
    p(className + "::" + className + "(" + ns + className + "* o) : ObjectWrap(), " + className.toLowerCase() + "(o) {};");
    p(className + "::~" + className + "() { delete " + className.toLowerCase() + "; };");
    p("");
    p("NAN_METHOD(" + className + "::New) {");
    p("  NanScope();");
    p("");
    p("  " + className + "* obj = new " + className + "();");
    p("  obj->Wrap(args.This());");
    p("");
    p("  NanReturnValue(args.This());");
    p("}");
    p("");
  }

  @Override public void exitCppClass(@NotNull nodewebkitwrapperParser.CppClassContext ctx) {
    p("");
    p("void " + className + "::Init(v8::Handle<v8::Object> exports) {");
    p("  NanScope();");
    p("");
    p("  // Prepare constructor template");
    p("  v8::Local<v8::FunctionTemplate> tpl = NanNew<v8::FunctionTemplate>(New);");
    p("  tpl->SetClassName(NanNew<v8::String>(\"" + className + "\"));");
    p("  tpl->InstanceTemplate()->SetInternalFieldCount(1);");
    p("");
    for (String staticMethod : staticMethods) {
      p("  tpl->Set(NanNew<v8::String>(\"" + staticMethod + "\"),");
      p("      NanNew<v8::FunctionTemplate>(" + staticMethod + ")->GetFunction());");
    }
    p("");
    p("  // Prototype");
    for (String method : methods) {
      p("  NanSetPrototypeTemplate(tpl, \"" + method + "\", NanNew<v8::String>(\"" + method + " prop?\"));");
    }
    p("");
    p("  NanAssignPersistent<v8::Function>(constructor, tpl->GetFunction());");
    p("  exports->Set(NanNew<v8::String>(\"" + className + "\"), tpl->GetFunction());");
    p("}");
  }

  @Override public void enterMethod(@NotNull nodewebkitwrapperParser.MethodContext ctx) {
    String methodName = ctx.Identifier().toString();
    boolean isStatic = ctx.STATIC() != null;
    boolean isVoid = ctx.type().typeName().VOID() != null;
    boolean isInstance = ctx.type().getText().equals(className + "*");
    if (isStatic && !isInstance) return;  // skip
    p("");
    if (isStatic) {
      staticMethods.add(methodName);
    } else {
      methods.add(methodName);
    }
    p("NAN_METHOD(" + className + "::" + methodName + ") {");
    p("  NanScope();");
    p("");
    if (!isStatic)
      p("  " + className + "* obj = ObjectWrap::Unwrap<" + className + ">(args.This());");
    if (isInstance) {
      p("  " + ns + className + "* " + className.toLowerCase() + " =");
      p("      " + ns + className + "::" + methodName + "(" + paramList(ctx.parameterList()) + ");");
      p("  v8::Local<v8::Function> cons = NanNew<v8::Function>(constructor);");
      p("  v8::Local<v8::Object> instance = cons->NewInstance();");
      p("");
      p("  " + className + "* obj = ObjectWrap::Unwrap<" + className + ">(instance);");
      p("  obj->" + className.toLowerCase() + " = " + className.toLowerCase() + ";");
    } else {
      if (!isVoid)
        System.out.print("  " + (ctx.type().CONST() != null ? "const " : "") + ctx.type().typeName().getText() + " result =");
      p("  obj->" + className.toLowerCase() + "->" + methodName + "(" + paramList(ctx.parameterList()) + ");");
    }
    p("");
    if (isVoid) {
      p("  NanReturnUndefined();");
    } else if (isInstance) {
      p("  NanReturnValue(instance);");
    } else {
      p("  NanReturnValue(" + wrap(ctx.type(), "result") + ");");
    }
    p("}");
  }

}