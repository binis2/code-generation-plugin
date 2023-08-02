package net.binis.codegen.compiler.plugin;

/*-
 * #%L
 * code-generator-plugin
 * %%
 * Copyright (C) 2021 - 2023 Binis Belev
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import com.sun.source.util.JavacTask;
import com.sun.source.util.Plugin;
import com.sun.source.util.TaskEvent;
import com.sun.source.util.TaskListener;
import com.sun.tools.javac.api.BasicJavacTask;
import com.sun.tools.javac.main.JavaCompiler;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.util.Context;
import net.binis.codegen.compiler.plugin.parser.CodeGenAttr;
import net.binis.codegen.compiler.plugin.parser.CodeGenParserFactory;
import net.binis.codegen.compiler.plugin.parser.CodeGenResolve;
import net.binis.codegen.factory.CodeFactory;
import net.binis.codegen.tools.Reflection;
import org.slf4j.Logger;

import javax.annotation.processing.ProcessingEnvironment;

public class CodeGenCompilerPlugin implements Plugin, TaskListener {

    private static final Logger log = org.slf4j.LoggerFactory.getLogger(CodeGenCompilerPlugin.class);

    private BasicJavacTask task;

    @Override
    public String getName() {
        return "BinisCodeGen";
    }

    @Override
    public void init(JavacTask task, String... args) {
        this.task = (BasicJavacTask) task;
        task.addTaskListener(this);

        CodeFactory.forceRegisterType(Context.class, CodeFactory.singleton(this.task.getContext()), null);
        CodeFactory.forceRegisterType(JavacTask.class, CodeFactory.singleton(task), null);
        CodeFactory.unregisterType(ProcessingEnvironment.class);

        patchCompilerClasses(this.task.getContext());
    }

    private void patchCompilerClasses(Context context) {
        CodeGenAttr.instance(context);
        CodeGenResolve.instance(context);
    }

    @Override
    public void started(TaskEvent e) {
        switch (e.getKind()) {
            case PARSE -> {
                var factory = CodeGenParserFactory.instance(task.getContext());
                Reflection.setFieldValue(JavaCompiler.instance(task.getContext()), "parserFactory", factory);
            }
            case ANNOTATION_PROCESSING_ROUND -> {
                if (Lookup._round.getAndIncrement() == 0) {
                    Lookup.analyzeAnnotations();
                }
            }
        }

        //log.info("Started: {} - {}", e.getKind(), withRes(e.getSourceFile(), source -> source.toUri().getPath()));
    }

    @Override
    public void finished(TaskEvent e) {
        switch (e.getKind()) {
            case PARSE ->
                e.getCompilationUnit().getTypeDecls().stream()
                        .filter(JCTree.JCClassDecl.class::isInstance)
                        .map(JCTree.JCClassDecl.class::cast)
                        .findFirst()
                        .ifPresent(type ->
                                Lookup._parsed.put(e.getCompilationUnit().getPackage().getPackageName().toString() + "." + type.name.toString(), e.getCompilationUnit()));
            case COMPILATION ->
                Lookup.clean();
        }

        //log.info("Finished: {} - {}", e.getKind(), withRes(e.getSourceFile(), source -> source.toUri().getPath()));
    }

}
