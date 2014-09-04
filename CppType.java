public class CppType {
  public boolean isVoid;
  public boolean isConst;
  public boolean isPointer = false;
  public boolean isPointerToPointer = false;
  public boolean isReference = false;
  public String name;

  public CppType(nodewebkitwrapperParser.TypeContext ctx) {
    isConst = ctx.CONST() != null;
    name = ctx.Identifier().toString();
    isVoid = name.equals("void");
  }

  public String unwrap(String var) {
    if (name.equals("int")) {
      return var + "->Uint32Value()";
    } else if (name.startsWith("string&")) {
      return "*v8::String::Utf8Value(" + var + "->ToString())";
    } else if (name.equals("bool")) {
      return var + "->BooleanValue()";
    } else if (name.equals("time_t")) {
      return var + "->Uint32Value() / 1000";
    } else {
      boolean ptr = name.charAt(name.length() - 1) == '*';
      boolean ref = name.charAt(name.length() - 1) == '&';
      return "(node::ObjectWrap::Unwrap<" + name.replaceAll("(\\*|&)", "") + ">(" + var + "->ToObject())->getNwcpValue())" + (ref ? "*" : "");
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