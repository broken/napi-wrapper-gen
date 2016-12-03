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
  public String v8Type() {
    return "v8::Object";
  }

  @Override
  public void outputResult() {
    o.i().p((isConst ? "const " : "") + fullName() + "* result =", false);
  }

  @Override
  public void outputReturn() {
    o.i().p("if (result == NULL) {").incIndent();
    o.i().p("info.GetReturnValue().SetNull();");
    o.decIndent().i().p("} else {").incIndent();
    outputWrap("result");
    o.p("");
    o.i().p("info.GetReturnValue().Set(instance);");
    o.decIndent().i().p("}");
  }

  @Override
  public void outputWrap(String var) {
    outputWrap(var, name.equals(cppClass.name));
  }

  @Override
  public void outputWrap(String var, boolean own) {
    o.i().p("v8::Local<v8::Object> instance = " + name + "::NewInstance();");
    o.i().p(name + "* r = Nan::ObjectWrap::Unwrap<" + name + ">(instance);");
    o.i().p("r->setNwcpValue(" + var + ", " + String.valueOf(own) + ");");
  }

  @Override
  public void outputUnwrap(String from, String to) {
    if (isPointer()) {
      o.i().p(fullName() + "* " + to + "(Nan::ObjectWrap::Unwrap<" + name + ">(" + from + "->ToObject())->getNwcpValue());");
    } else {
      o.i().p(fullName() + "* " + to + "tmp(Nan::ObjectWrap::Unwrap<" + name + ">(" + from + "->ToObject())->getNwcpValue());");
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
