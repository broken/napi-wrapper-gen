import java.util.Iterator;
import java.util.Stack;

import org.antlr.v4.runtime.TokenStream;
import org.antlr.v4.runtime.misc.Interval;
import org.antlr.v4.runtime.misc.NotNull;

public class HeaderWrapperListener extends nodewebkitwrapperBaseListener {
  nodewebkitwrapperParser parser;
  CppClass cppClass;
  CppNamespace cppNamespace = new CppNamespace();

  public HeaderWrapperListener(nodewebkitwrapperParser p) {
    parser = p;
  }

  private void p(String s) {
    System.out.println(s);
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
    p("#ifndef " + cppClass.name + "_wrap_h");
    p("#define " + cppClass.name + "_wrap_h");
    p("");
    p("#include <node.h>");
    p("#include <nan.h>");
    p("#include \"" + cppClass.name + ".h\"");
    p("");
    p("class " + cppClass.name + " : public node::ObjectWrap {");
    p(" public:");
    p("  static void Init(v8::Handle<v8::Object> exports);");
    p("  static NAN_METHOD(NewInstance);");
    p("");
    p(" private:");
    p("  " + cppClass.name + "();");
    p("  explicit " + cppClass.name + "(" + cppNamespace + cppClass.name + "* " + cppClass.name.toLowerCase() + ");");
    p("  ~" + cppClass.name + "();");
    p("");
    p("  static NAN_METHOD(New);");
    p("");
    p("  " + cppNamespace + cppClass.name + "* getNwcpValue() const { return " + cppClass.name.toLowerCase() + "; }");
    p("");
    for (CppMethod m : cppClass.methods) {
      if (m.isStatic && !m.isInstanceOf(cppClass)) continue;  // skip
      if (m.returnType.isUnknownType(cppClass)) continue;
      boolean cannotHandleArg = false;
      for (CppType t : m.args) {
        cannotHandleArg |= t.isUnknownType(cppClass);
      }
      if (cannotHandleArg) continue;
      p("  static NAN_METHOD(" + m.name + ");");
    }
    p("");
    p("  static v8::Persistent<v8::Function> constructor;");
    p("  " + cppNamespace + cppClass.name + "* " + cppClass.name.toLowerCase() + ";");
    p("};");
    p("");
    p("#endif");
  }

  @Override public void enterMethod(@NotNull nodewebkitwrapperParser.MethodContext ctx) {
    cppClass.methods.add(new CppMethod(ctx));
  }


}