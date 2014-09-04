import java.util.ArrayList;
import java.util.List;

public class CppMethod {
  public boolean isStatic = false;
  public boolean isConst = false;
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
  }
}