package com.dogatech.nodewebkitwrapper.prototype.type;

import java.util.Set;

import com.dogatech.nodewebkitwrapper.grammar.nodewebkitwrapperParser;
import com.dogatech.nodewebkitwrapper.io.Outputter;


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
    o.i().p((isConst ? "const " : "") + fullName() + "* result =", false);
  }

  @Override
  public void outputReturn() {
    o.i().p("if (result == NULL) {").incIndent();
    o.i().p("return info.Env().Null();");
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
    o.i().p("Napi::Object instance = " + name + "::NewInstance(info.Env());");
    o.i().p(name + "* r = Napi::ObjectWrap<" + name + ">::Unwrap(instance);");
    o.i().p("r->setWrappedValue(" + var + ", " + String.valueOf(own) + ");");
  }

  @Override
  public void outputUnwrap(String from, String to) {
    o.i().p("if (!" + from + ".IsObject()) {").incIndent();
    o.i().p("Napi::TypeError::New(info.Env(), \"TypeError: Object expected (for " + from + ")\").ThrowAsJavaScriptException();");
    o.i().p("return", false);
    if (!isInVoidMethod) o.p(" info.Env().Null()", false);
    o.p(";");
    o.decIndent().i().p("}");
    if (isPointer()) {
      o.i().p(fullName() + "* " + to + "(Napi::ObjectWrap<" + name + ">::Unwrap(" + from + ".As<Napi::Object>())->getWrappedValue());");
    } else {
      o.i().p(fullName() + "* " + to + "tmp(Napi::ObjectWrap<" + name + ">::Unwrap(" + from + ".As<Napi::Object>())->getWrappedValue());");
      o.i().p(fullName() + "& " + to + " = *" + to + "tmp;");
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
  public String fullName() {
    return "dogatech::soulsifter::" + name;
  }
}
