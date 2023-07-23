package net.binis.codegen.compiler.plugin;

import com.sun.source.util.JavacTask;
import com.sun.source.util.Plugin;
import com.sun.source.util.TaskEvent;
import com.sun.source.util.TaskListener;
import com.sun.tools.javac.api.BasicJavacTask;
import com.sun.tools.javac.main.JavaCompiler;
import com.sun.tools.javac.util.Context;
import net.binis.codegen.compiler.plugin.parser.CodeGenAttr;
import net.binis.codegen.compiler.plugin.parser.CodeGenParserFactory;
import net.binis.codegen.compiler.plugin.parser.CodeGenResolve;
import net.binis.codegen.tools.Reflection;
import org.slf4j.Logger;

import static net.binis.codegen.tools.Tools.withRes;
//import com.sun.tools.javac.api.BasicJavacTask;
//import com.sun.tools.javac.util.Log;

public class CodeGenCompilerPlugin implements Plugin, TaskListener {

    private static final Logger log = org.slf4j.LoggerFactory.getLogger(CodeGenCompilerPlugin.class);

    private BasicJavacTask task;

    @Override
    public String getName() {
        return "BinisCodeGen";
    }

    @Override
    public void init(JavacTask task, String... args) {
////        var context = ((BasicJavacTask) task).getContext();
////        Log.instance(context)
////                .printRawLines(Log.WriterKind.NOTICE, "Hello from " + getName());
        this.task = (BasicJavacTask) task;
        task.addTaskListener(this);

        patchCompilerClasses(this.task.getContext());
    }

    private void patchCompilerClasses(Context context) {
        CodeGenAttr.instance(context);
        CodeGenResolve.instance(context);
    }

    @Override
    public void started(TaskEvent e) {
        switch (e.getKind()) {
            case PARSE:
                var factory = CodeGenParserFactory.instance(task.getContext());

                //factory.setTaskEvent( e );
                Reflection.setFieldValue(JavaCompiler.instance(task.getContext()), "parserFactory", factory);
                break;

            case ENTER:
//                initialize(e);
//                // add the fragments created during parsing
//                addFileFragments(e);
                break;

            case ANALYZE:
//                initialize(e);
//                // Add extension methods to javac's array type
//                extendArrayType(e);
                break;
        }

        log.info("Started: {} - {}", e.getKind(), withRes(e.getSourceFile(), source -> source.toUri().getPath()));
    }

    @Override
    public void finished(TaskEvent e) {
        log.info("Finished: {} - {}", e.getKind(), withRes(e.getSourceFile(), source -> source.toUri().getPath()));
    }

}
