package net.binis.codegen.compiler.plugin.parser;

import com.sun.tools.javac.parser.JavacParser;
import com.sun.tools.javac.parser.Lexer;
import com.sun.tools.javac.parser.ParserFactory;

public class CodeGenJavacParser extends JavacParser {

    protected CodeGenJavacParser(ParserFactory fac, Lexer S, boolean keepDocComments, boolean keepLineMap, boolean keepEndPositions) {
        super(fac, S, keepDocComments, keepLineMap, keepEndPositions);
    }

    protected CodeGenJavacParser(ParserFactory fac, Lexer S, boolean keepDocComments, boolean keepLineMap, boolean keepEndPositions, boolean parseModuleInfo) {
        super(fac, S, keepDocComments, keepLineMap, keepEndPositions, parseModuleInfo);
    }
}
