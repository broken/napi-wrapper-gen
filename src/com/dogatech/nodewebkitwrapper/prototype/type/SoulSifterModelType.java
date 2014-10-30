package com.dogatech.nodewebkitwrapper.prototype.type;

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
            name.startsWith("SearchUtil") ||
            name.startsWith("Song") ||
            name.startsWith("Style"));
  }

  @Override
  public void outputResult() {
    o.i().p((isConst ? "const " : "") + "dogatech::soulsifter::" + nameSansRef() + " result =", false);
  }

  @Override
  public void outputReturn() {
    String typeName = name.replaceAll("(&|\\*)", "");
    o.i().p("if (result == NULL) NanReturnUndefined();");
    outputWrap("result");
    o.p("");
    o.i().p("NanReturnValue(instance);");
  }

  @Override
  public void outputWrap(String var) {
    String typeName = name.replaceAll("(&|\\*)", "");
    o.i().p("v8::Local<v8::Object> instance = " + typeName + "::NewInstance();");
    o.i().p(name + " r = ObjectWrap::Unwrap<" + typeName + ">(instance);");
    o.i().p("r->setNwcpValue(" + var + ", true);");
  }
}