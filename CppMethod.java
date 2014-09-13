import java.util.ArrayList;
import java.util.List;

public class CppMethod {
  public boolean isStatic = false;
  public boolean isConst = false;
  public boolean isClassType;
  public CppType returnType;
  public String name;
  public List<CppType> args = new ArrayList<CppType>();

  public CppMethod(nodewebkitwrapperParser.MethodContext ctx) {
    isStatic = ctx.STATIC() != null;
    isConst = ctx.CONST() != null;
    name = ctx.Identifier().toString();
    returnType = new CppType(ctx.type());
    for (nodewebkitwrapperParser.ParameterContext p : ctx.parameterList().parameter()) {
      args.add(new CppType(p.type()));
    }

    isClassType = !returnType.name.equals("int")
          && !returnType.name.startsWith("string")
          && !returnType.name.startsWith("vector")
          && !returnType.name.startsWith("ResultSetIterator")
          && !returnType.name.equals("bool")
          && !returnType.name.equals("time_t");
  }

  public boolean isInstanceOf(CppClass cppClass) {
    return returnType.name.equals(cppClass.name + "*");
  }
}