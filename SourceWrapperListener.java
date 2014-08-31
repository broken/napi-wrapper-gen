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

  private String wrap(String type, String name) {
    if (type.equals("int")) {
      return "Number::New(" + name + ")";
    } else if (type.equals("string")) {
      return "String::New(" + name + ".c_str(), " + name + ".length())";
    } else {
      return "";
    }
  }

  private String unwrap(String type, String name) {
    if (type.equals("int")) {
      return name + "->Uint32Value()";
    } else if (type.equals("string")) {
      return "*v8::String::Utf8Value(" + name + "->ToString())";
    } else {
      return "";
    }
  }

  private String paramList(nodewebkitwrapperParser.ParameterListContext ctx) {
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < ctx.parameter().size(); ++i) {
      sb.append(unwrap(ctx.parameter().get(i).type().getText(), "args[" + i + "]"));
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
    p("#include \"" + className + ".h\"");
    p("#include \"" + className + "_wrap.h\"");
    p("");
    p("using namespace v8;");
    p("");
    p("Persistent<Function> " + className + "::constructor;");
    p("");
    p(className + "::" + className + "() : ObjectWrap(), " + className.toLowerCase() + "(new " + ns + className + "()) {};");
    p(className + "::" + className + "(" + ns + className + "* o) : ObjectWrap(), " + className.toLowerCase() + "(o) {};");
    p(className + "::~" + className + "() { delete " + className.toLowerCase() + "; };");
    p("");
    p("Handle<Value> " + className + "::New(const Arguments& args) {");
    p("  Isolate* isolate = Isolate::GetCurrent();");
    p("  HandleScope scope(isolate);");
    p("");
    p("  " + className + "* obj = new " + className + "();");
    p("  obj->Wrap(args.This());");
    p("");
    p("  return args.This();");
    p("}");
    p("");
  }

  @Override public void exitCppClass(@NotNull nodewebkitwrapperParser.CppClassContext ctx) {
    p("");
    p("void " + className + "::Init(Handle<Object> exports) {");
    p("  Isolate* isolate = Isolate::GetCurrent();");
    p("");
    p("  // Prepare constructor template");
    p("  Local<FunctionTemplate> tpl = FunctionTemplate::New(New);");
    p("  tpl->SetClassName(String::NewSymbol(\"" + className + "\"));");
    p("  tpl->InstanceTemplate()->SetInternalFieldCount(1);");
    p("");
    for (String staticMethod : staticMethods) {
      p("  tpl->Set(String::NewSymbol(\"" + staticMethod + "\"),");
      p("      FunctionTemplate::New(" + staticMethod + ")->GetFunction());");
    }
    p("");
    p("  // Prototype");
    for (String method : methods) {
      p("  tpl->PrototypeTemplate()->Set(String::NewSymbol(\"" + method + "\"),");
      p("      FunctionTemplate::New(" + method + ")->GetFunction());");
    }
    p("");
    p("  constructor = Persistent<Function>::New(isolate, tpl->GetFunction());");
    p("  exports->Set(String::NewSymbol(\"" + className + "\"), constructor);");
    p("}");
  }

  @Override public void enterMethod(@NotNull nodewebkitwrapperParser.MethodContext ctx) {
    String methodName = ctx.Identifier().toString();
    boolean isStatic = ctx.STATIC() != null;
    boolean isVoid = ctx.type().VOID() != null;
    boolean isInstance = ctx.type().getText().equals(className + "*");
    if (isStatic && !isInstance) return;  // skip
    p("");
    if (isStatic) {
      staticMethods.add(methodName);
    } else {
      methods.add(methodName);
    }
    p("Handle<Value> " + className + "::" + methodName + "(const Arguments& args) {");
    p("  Isolate* isolate = Isolate::GetCurrent();");
    p("  HandleScope scope(isolate);");
    p("");
    if (!isStatic)
      p("  " + className + "* obj = ObjectWrap::Unwrap<" + className + ">(args.This());");
    if (isInstance) {
      p("  " + ns + className + "* " + className.toLowerCase() + " =");
      p("      " + ns + className + "::" + methodName + "(" + paramList(ctx.parameterList()) + ");");
      p("  const unsigned argc = 0;");
      p("  Handle<Value> argv[argc] = { };");
      p("  Local<Object> instance = constructor->NewInstance(argc, argv);");
      p("");
      p("  " + className + "* obj = ObjectWrap::Unwrap<" + className + ">(instance);");
      p("  obj->" + className.toLowerCase() + " = " + className.toLowerCase() + ";");
    } else {
      if (!isVoid)
        System.out.print("  " + ctx.type().getText() + " result =");
      p("  obj->" + className.toLowerCase() + "->" + methodName + "(" + paramList(ctx.parameterList()) + ");");
    }
    p("");
    if (isVoid) {
      p("  return Undefined();");
    } else if (isInstance) {
      p("  return scope.Close(instance);");
    } else {
      p("  return scope.Close(" + wrap(ctx.type().getText(), "result") + ");");
    }
    p("}");
  }

}