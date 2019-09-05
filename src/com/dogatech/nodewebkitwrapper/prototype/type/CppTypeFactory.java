package com.dogatech.nodewebkitwrapper.prototype.type;

import java.util.LinkedList;
import java.util.List;

import org.antlr.v4.runtime.tree.TerminalNode;

import com.dogatech.nodewebkitwrapper.grammar.nodewebkitwrapperParser;
import com.dogatech.nodewebkitwrapper.io.Outputter;
import com.dogatech.nodewebkitwrapper.prototype.CppClass;
import com.dogatech.nodewebkitwrapper.prototype.CppMethod;


public class CppTypeFactory {
  private static CppTypeFactory instance = null;
  private CppType[] types = {
    new VoidType(),
    new ResultSetIteratorType(),
    new SoulSifterModelType(),
    new VectorType(),
    new SetType(),
    new BooleanType(),
    new IntegerType(),
    new NumberType(),
    new StringType(),
    new TimeTType()
  };

  protected CppTypeFactory() { }

  public static CppTypeFactory instance() {
    if (instance == null) {
      instance = new CppTypeFactory();
    }
    return instance;
  }

  public CppType createType(nodewebkitwrapperParser.TypeContext ctx, CppClass cppClass, Outputter out) {
    CppType t = null;
    try {
      for (CppType type : types) {
        if (type.isType(ctx)) {
          t = type.getClass().newInstance();
          t.init(ctx.Identifier().toString(), cppClass, out);
        }
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
    if (t != null) t.isConst = ctx.CONST() != null;
    if (t != null && ctx.Modifier() != null) {
      for (TerminalNode n : ctx.Modifier()) {
        t.modifiers.add(n.getText());
      }
    }
    if (t != null && ctx.generic() != null) {
      for (nodewebkitwrapperParser.TypeContext tc : ctx.generic().typeList().type()) {
        CppType gt = createType(tc, cppClass, out);
        if (gt == null) return null;
        t.generics.add(gt);
      }
    }
    return t;
  }

  public CppType createType(String name, CppClass cppClass, Outputter out) {
    try {
      for (CppType t : types) {
        if (t.isType(name)) {
          CppType type = t.getClass().newInstance();
          type.init(name, cppClass, out);
          return type;
        }
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
    return null;
  }
}
