import java.util.Iterator;
import java.util.Stack;

import org.antlr.v4.runtime.TokenStream;
import org.antlr.v4.runtime.misc.Interval;
import org.antlr.v4.runtime.misc.NotNull;

public class HeaderWrapperListener extends nodewebkitwrapperBaseListener {
  nodewebkitwrapperParser parser;
  String className;
  String ns;
  Stack<String> namespaces;

  public HeaderWrapperListener(nodewebkitwrapperParser p) {
    parser = p;
    ns = "";
    namespaces = new Stack<String>();
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
    p("#ifndef " + className + "_wrap_h");
    p("#define " + className + "_wrap_h");
    p("");
    p("#include <node.h>");
    p("#include <nan.h>");
    p("#include \"" + className + ".h\"");
    p("");
    p("class " + className + " : public node::ObjectWrap {");
    p(" public:");
    p("  static void Init(v8::Handle<v8::Object> exports);");
    p("  static NAN_METHOD(NewInstance);");
    p("");
    p(" private:");
    p("  " + className + "();");
    p("  explicit " + className + "(" + ns + className + "* " + className.toLowerCase() + ");");
    p("  ~" + className + "();");
    p("");
    p("  static NAN_METHOD(New);");
    p("");
    p("  " + ns + className + "* getNwcpValue() const { return " + className.toLowerCase() + "; }");
    p("");
  }

  @Override public void exitCppClass(@NotNull nodewebkitwrapperParser.CppClassContext ctx) {
    p("");
    p("  static v8::Persistent<v8::Function> constructor;");
    p("  " + ns + className + "* " + className.toLowerCase() + ";");
    p("};");
    p("");
    p("#endif");
  }

  @Override public void enterMethod(@NotNull nodewebkitwrapperParser.MethodContext ctx) {
    boolean isStatic = ctx.STATIC() != null;
    boolean isInstance = ctx.type().getText().equals(className + "*");
    if (isStatic && !isInstance) return;  // skip

    p("  static NAN_METHOD(" + ctx.Identifier() + ");");
  }


}