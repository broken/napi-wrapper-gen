package com.dogatech.napiwrapper;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.*;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.FileSystems;

import com.dogatech.napiwrapper.grammar.napiwrapperLexer;
import com.dogatech.napiwrapper.grammar.napiwrapperParser;
import com.dogatech.napiwrapper.io.FileOutputter;
import com.dogatech.napiwrapper.listener.HeaderWrapperListener;
import com.dogatech.napiwrapper.listener.SourceWrapperListener;


/**
 * TODO
 */
public class WrapperTool {
  public static void main(String[] args) throws Exception {
    // header files to process
    for (int i = 0; i < args.length; ++i) {
      String inputFile = args[i];
      if (!inputFile.endsWith(".h")) {
        System.err.println("Input file must be header file.");
        continue;
      }
      InputStream is = new FileInputStream(inputFile);
      String outputHeader = inputFile.replace(".h", "_wrap.h");
      String outputSource = inputFile.replace(".h", "_wrap.cpp");

      ANTLRInputStream input = new ANTLRInputStream(is);

      napiwrapperLexer lexer = new napiwrapperLexer(input);
      CommonTokenStream tokens = new CommonTokenStream(lexer);
      napiwrapperParser parser = new napiwrapperParser(tokens);
      ParseTree tree = parser.header(); // parse

      ParseTreeWalker walker = new ParseTreeWalker(); // create standard walker
      HeaderWrapperListener extractor = new HeaderWrapperListener(parser, new FileOutputter(new FileOutputStream(outputHeader)));
      walker.walk(extractor, tree); // initiate walk of tree with listener

      SourceWrapperListener extractor2 = new SourceWrapperListener(parser, new FileOutputter(new FileOutputStream(outputSource)));
      walker.walk(extractor2, tree); // initiate walk of tree with listener
    }
  }
}