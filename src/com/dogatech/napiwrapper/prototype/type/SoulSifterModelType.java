package com.dogatech.napiwrapper.prototype.type;

import java.util.Set;

import com.dogatech.napiwrapper.grammar.napiwrapperParser;
import com.dogatech.napiwrapper.io.Outputter;
import com.dogatech.napiwrapper.prototype.CppMethod;


public class SoulSifterModelType extends CppType {

  @Override
  public boolean isType(String name) {
    return (name.startsWith("Album") ||
            name.startsWith("AlbumPart") ||
            name.startsWith("AudioAnalyzer") ||
            name.startsWith("BasicGenre") ||
            name.startsWith("Mix") ||
            name.startsWith("MusicManager") ||
            name.startsWith("MusicVideo") ||
            name.startsWith("Playlist") ||
            name.startsWith("PlaylistEntry") ||
            name.startsWith("SearchUtil") ||
            name.startsWith("Song") ||
            name.startsWith("Style"));
  }

  @Override
  public void outputResult() {
    o.i().p((isConst ? "const " : "") + fullName(true) + "* result =", false);
  }

  @Override
  public void outputReturn() {
    o.i().p("if (result == NULL) {").incIndent();
    o.i().p("return env.Null();");
    o.decIndent().i().p("} else {").incIndent();
    outputWrap("result");
    o.i().p("return instance;");
    o.decIndent().i().p("}");
  }

  @Override
  public void outputWrap(String var) {
    outputWrap(var, name.equals(cppClass.name));
  }

  @Override
  public void outputWrap(String var, boolean own) {
    o.i().p("Napi::Object instance = " + name + "::NewInstance(env);");
    o.i().p(name + "* r = Napi::ObjectWrap<" + name + ">::Unwrap(instance);");
    o.i().p("r->setWrappedValue(" + var + ", " + String.valueOf(own) + ");");
  }

  @Override
  public void outputUnwrap(String from, String to, CppMethod.MethodType mt) {
    o.i().p("if (!" + from + ".IsObject()) {").incIndent();
    mt.errOut("TypeError: Object expected (for " + from + ")");
    o.decIndent().i().p("}");
    if (isPointer()) {
      o.i().p(fullName() + " " + to + "(Napi::ObjectWrap<" + name + ">::Unwrap(" + from + ".As<Napi::Object>())->getWrappedValue());");
    } else {
      o.i().p(fullName(true) + "* " + to + "tmp(Napi::ObjectWrap<" + name + ">::Unwrap(" + from + ".As<Napi::Object>())->getWrappedValue());");
      o.i().p(fullName(true) + "& " + to + " = *" + to + "tmp;");
    }
  }

  @Override
  public Set<String> requiredHeaders() {
    Set<String> s = super.requiredHeaders();
    s.add(name + ".h");
    s.add(name + "_wrap.h");
    return s;
  }

  @Override
  public String fullName(boolean rmMod) {
    StringBuilder sb = new StringBuilder();
    sb.append("dogatech::soulsifter::");
    sb.append(name);
    if (generics.size() > 0) {
      sb.append("<");
      for (int i = 0; i < generics.size(); ++i) {
        if (i > 0) sb.append(", ");
        sb.append(generics.get(i).fullName());
      }
      sb.append(">");
    }
    if (!rmMod) {
      for (String m : modifiers) {
        sb.append(m);
      }
    }
    return sb.toString();
  }
}
