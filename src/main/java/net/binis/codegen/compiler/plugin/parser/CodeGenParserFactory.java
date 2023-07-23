package net.binis.codegen.compiler.plugin.parser;

import com.sun.source.util.TaskEvent;
import com.sun.source.util.TaskListener;
import com.sun.tools.javac.parser.*;
import com.sun.tools.javac.util.Context;

//import java.nio.CharBuffer;
//import javax.tools.JavaFileObject;
//
//import com.sun.tools.javac.util.JCDiagnostic;
import net.binis.codegen.tools.Reflection;


//import static com.sun.tools.javac.parser.Tokens.TokenKind.STRINGLITERAL;


public class CodeGenParserFactory extends ParserFactory {

    private TaskEvent _taskEvent;

    public static CodeGenParserFactory instance(Context ctx) {
        ParserFactory parserFactory = ctx.get(parserFactoryKey);
        if (!(parserFactory instanceof CodeGenParserFactory)) {
            ctx.put(parserFactoryKey, (ParserFactory) null);
            parserFactory = new CodeGenParserFactory(ctx);
        }

        return (CodeGenParserFactory) parserFactory;
    }

    private CodeGenParserFactory(Context ctx) {
        super(ctx);
//        _preprocessor = Preprocessor.instance(ctx);
//        ReflectUtil.field(this, "scannerFactory").set(ManScannerFactory.instance(ctx, this));
    }

    @Override
    public JavacParser newParser(CharSequence input, boolean keepDocComments, boolean keepEndPos, boolean keepLineMap) {
        return newParser(input, keepDocComments, keepEndPos, keepLineMap, false);
    }

    public JavacParser newParser(CharSequence input, boolean keepDocComments, boolean keepEndPos, boolean keepLineMap, boolean parseModuleInfo) {
//        input = _preprocessor.process(_taskEvent.getSourceFile(), input);
//        mapInput(_taskEvent.getSourceFile(), input);
        var lexer = ((ScannerFactory) Reflection.getFieldValue(this, "scannerFactory")).newScanner(input, keepDocComments);
        return new CodeGenJavacParser(this, lexer, keepDocComments, keepLineMap, keepEndPos, parseModuleInfo);
    }

//    private void mapInput(JavaFileObject sourceFile, CharSequence input) {
//        fileToProcessedInput.put(sourceFile.getName(), input);
//    }
//
//    @Override
//    public void setTaskEvent(TaskEvent e) {
//        _taskEvent = e;
//    }

//    /**
//     * Override ScannerFactory so we can examine tokens as they are read.  This is purely a performance measure to avoid
//     * having to tokenize each source file twice.
//     */
//    public static class ManScannerFactory extends ScannerFactory {
//        private final ManParserFactory_17 _parserFactory;
//
//        public static ScannerFactory instance(Context ctx, ManParserFactory_17 parserFactory) {
//            ScannerFactory scannerFactory = ctx.get(scannerFactoryKey);
//            if (!(scannerFactory instanceof ManScannerFactory)) {
//                ctx.put(scannerFactoryKey, (ScannerFactory) null);
//                scannerFactory = new ManScannerFactory(ctx, parserFactory);
//            }
//
//            return scannerFactory;
//        }
//
//        private ManScannerFactory(Context ctx, ManParserFactory_17 parserFactory) {
//            super(ctx);
//            _parserFactory = parserFactory;
//        }
//
//        public Scanner newScanner(CharSequence input, boolean keepDocComments) {
//            if (input instanceof CharBuffer) {
//                CharBuffer buf = (CharBuffer) input;
//                if (keepDocComments) {
//                    return new ManScanner(this, new ManJavadocTokenizer(this, buf));
//                } else {
//                    return new ManScanner(this, buf);
//                }
//            } else {
//                char[] array = input.toString().toCharArray();
//                return newScanner(array, array.length, keepDocComments);
//            }
//        }
//
//        public Scanner newScanner(char[] input, int inputLength, boolean keepDocComments) {
//            if (keepDocComments) {
//                return new ManScanner(this, new ManJavadocTokenizer(this, input, inputLength));
//            } else {
//                return new ManScanner(this, input, inputLength);
//            }
//        }
//
//        private static class ManJavadocTokenizer extends JavadocTokenizer {
//            private final ManScannerFactory _scannerFactory;
//
//            ManJavadocTokenizer(ManScannerFactory manScannerFactory, CharBuffer buf) {
//                super(manScannerFactory, buf);
//                _scannerFactory = manScannerFactory;
//            }
//
//            ManJavadocTokenizer(ManScannerFactory manScannerFactory, char[] input, int inputLength) {
//                super(manScannerFactory, input, inputLength);
//                _scannerFactory = manScannerFactory;
//            }
//
//            protected Tokens.Comment processComment(int pos, int endPos, Tokens.Comment.CommentStyle style) {
//                Tokens.Comment comment = super.processComment(pos, endPos, style);
//                char[] buf = getRawCharacters(pos, endPos);
//                FragmentProcessor.instance().processComment(
//                        _scannerFactory._parserFactory._taskEvent.getSourceFile(), pos, new String(buf), style);
//                return comment;
//            }
//
//            public Tokens.Token readToken() {
//                Tokens.Token token = super.readToken();
//                if (token.kind == STRINGLITERAL) {
//                    // todo: passing raw characters means we must parse string literal escaped chars esp. '"', '\n', unicode
//                    char[] buf = getRawCharacters(token.pos, token.endPos);
//                    FragmentProcessor.instance().processString(
//                            ((ManScannerFactory) ReflectUtil.field(this, "fac").get())._parserFactory._taskEvent.getSourceFile(), token.pos, new String(buf));
//                }
//                return token;
//            }
//
//            /**
//             * Fixes escaped $ char for string templates
//             */
//            @Override
//            protected void lexError(int pos, JCDiagnostic.Error key) {
//                if (tk == STRINGLITERAL &&
//                        '$' == getRawCharacters(pos, pos + 1)[0] &&
//                        '\\' == getRawCharacters(pos - 1, pos)[0]) {
//                    sb.deleteCharAt(sb.length() - 1); // delete the escape char
//                    putThenNext(); // add the '$' char
//                    super.lexError(pos, key);
//                    tk = STRINGLITERAL; // mark the token as a legal string literal, no error here
//                    return;
//                }
//                super.lexError(pos, key);
//            }
//        }
//
//        private static class ManScanner extends Scanner {
//            ManScanner(ManScannerFactory manScannerFactory, JavaTokenizer manJavadocTokenizer) {
//                super(manScannerFactory, manJavadocTokenizer);
//            }
//
//            ManScanner(ManScannerFactory fac, char[] buf, int len) {
//                this(fac, new ManJavaTokenizer(fac, buf, len));
//            }
//
//            ManScanner(ManScannerFactory fac, CharBuffer buf) {
//                this(fac, new ManJavaTokenizer(fac, buf));
//            }
//
//            private static class ManJavaTokenizer extends JavaTokenizer {
//                ManJavaTokenizer(ManScannerFactory fac, char[] buf, int len) {
//                    super(fac, buf, len);
//                }
//
//                ManJavaTokenizer(ManScannerFactory fac, CharBuffer buf) {
//                    super(fac, buf);
//                }
//
//                protected Tokens.Comment processComment(int pos, int endPos, Tokens.Comment.CommentStyle style) {
//                    Tokens.Comment comment = super.processComment(pos, endPos, style);
//                    char[] buf = getRawCharacters(pos, endPos);
//                    FragmentProcessor.instance().processComment(
//                            ((ManScannerFactory) fac)._parserFactory._taskEvent.getSourceFile(), pos, new String(buf), style);
//                    return comment;
//                }
//
//                public Tokens.Token readToken() {
//                    Tokens.Token token = super.readToken();
//                    if (token.kind == STRINGLITERAL) {
//                        char[] buf = getRawCharacters(token.pos, token.endPos);
//                        FragmentProcessor.instance().processString(
//                                ((ManScannerFactory) fac)._parserFactory._taskEvent.getSourceFile(), token.pos, new String(buf));
//                    }
//                    return token;
//                }
//
//                /**
//                 * Fixes escaped $ char for string templates
//                 */
//                @Override
//                protected void lexError(int pos, JCDiagnostic.Error key) {
//                    if (tk == STRINGLITERAL &&
//                            '$' == getRawCharacters(pos, pos + 1)[0] &&
//                            '\\' == getRawCharacters(pos - 1, pos)[0]) {
//                        sb.deleteCharAt(sb.length() - 1); // remove escape char since in Java 17 it is kept in the buffer, where unicode parsing escapes it
//                        putThenNext();
//                        super.lexError(pos, key);
//                        tk = STRINGLITERAL;
//                        return;
//                    }
//                    super.lexError(pos, key);
//                }
//            }
//        }
//    }
}
