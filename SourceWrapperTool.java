import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.*;

import java.io.FileInputStream;
import java.io.InputStream;

public class SourceWrapperTool {
    public static void main(String[] args) throws Exception {
        String inputFile = null;
        if ( args.length>0 ) inputFile = args[0];
        InputStream is = System.in;
        if ( inputFile!=null ) {
            is = new FileInputStream(inputFile);
        }
        ANTLRInputStream input = new ANTLRInputStream(is);

        nodewebkitwrapperLexer lexer = new nodewebkitwrapperLexer(input);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        nodewebkitwrapperParser parser = new nodewebkitwrapperParser(tokens);
        ParseTree tree = parser.header(); // parse

        ParseTreeWalker walker = new ParseTreeWalker(); // create standard walker
        SourceWrapperListener extractor = new SourceWrapperListener(parser);
        walker.walk(extractor, tree); // initiate walk of tree with listener
    }
}