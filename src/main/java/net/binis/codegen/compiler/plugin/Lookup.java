package net.binis.codegen.compiler.plugin;

/*-
 * #%L
 * code-generator-plugin
 * %%
 * Copyright (C) 2021 - 2026 Binis Belev
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

import com.sun.source.tree.CompilationUnitTree;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.util.Context;
import com.sun.tools.javac.util.List;
import com.sun.tools.javac.util.Names;
import com.sun.tools.javac.code.Flags;
import net.binis.codegen.compiler.TreeMaker;
import net.binis.codegen.compiler.utils.ElementUtils;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import static java.util.Objects.nonNull;
import static net.binis.codegen.tools.Reflection.loadClass;

public class Lookup {

    public static Map<String, CompilationUnitTree> _parsed = new ConcurrentHashMap<>();

    public static AtomicInteger _round = new AtomicInteger();


    public static void analyzeAnnotations() {
        var inherited = new HashMap<String, JCTree.JCClassDecl>();
        _parsed.forEach((name, unit) ->
            unit.getTypeDecls().stream()
                    .filter(JCTree.JCClassDecl.class::isInstance)
                    .map(JCTree.JCClassDecl.class::cast)
                    .filter(tree -> (tree.mods.flags & Flags.ANNOTATION) == Flags.ANNOTATION)
                    .filter(tree -> nonNull(tree.implementing))
                    .filter(tree -> tree.implementing.nonEmpty())
                    .forEach(tree -> inherited.put(name, tree)));
        inherited.forEach((name, type) -> {
            var list = List.<JCTree.JCExpression>nil();
            var methods = type.defs.stream()
                    .filter(JCTree.JCMethodDecl.class::isInstance)
                    .map(JCTree.JCMethodDecl.class::cast)
                    .map(m -> m.name.toString())
                    .collect(HashSet::new, HashSet::add, HashSet::addAll);
            for (var impl : type.implementing) {
                if (impl instanceof JCTree.JCIdent ident && nonNull(ident.sym)) {
                    var className = ident.sym.getQualifiedName().toString();
                    var parent = _parsed.get(className);
                    if (nonNull(parent)) {
                        var pType = parent.getTypeDecls().stream()
                                .filter(JCTree.JCClassDecl.class::isInstance)
                                .map(JCTree.JCClassDecl.class::cast)
                                .filter(tree -> tree.name.toString().equals(ident.name.toString()))
                                .findFirst();
                        if (pType.isPresent()) {
                            pType.get().defs.stream()
                                    .filter(JCTree.JCMethodDecl.class::isInstance)
                                    .map(JCTree.JCMethodDecl.class::cast)
                                    .forEach(method -> {
                                        if (!methods.contains(method.name.toString())) {
                                            type.defs = type.defs.append(method);
                                            methods.add(method.name.toString());
                                        }
                                    });
                        } else {
                            list = list.append(impl);
                        }
                    } else {
                        var cls = loadClass(className);
                        if (nonNull(cls) && cls.isAnnotation()) {
                            var m = TreeMaker.create();
                            var maker = (com.sun.tools.javac.tree.TreeMaker) m.getInstance();
                            var context = (Context) m.getContext();
                            for (var method : cls.getDeclaredMethods()) {
                                if (!methods.contains(method.getName())) {
                                    type.defs = type.defs.append(maker.MethodDef(maker.Modifiers(0),
                                            Names.instance(context).fromString(method.getName()),
                                            (JCTree.JCExpression) ElementUtils.classToExpression(method.getReturnType()).getInstance(),
                                            List.nil(),
                                            List.nil(),
                                            List.nil(),
                                            null,
                                            nonNull(method.getDefaultValue()) ? (JCTree.JCExpression) ElementUtils.calcExpression(m, method.getDefaultValue()).getInstance() : null));
                                    methods.add(method.getName());
                                }
                            }
                        } else {
                            list = list.append(impl);
                        }
                    }
                } else {
                    list = list.append(impl);
                }
            }
            type.implementing = list;
        });
    }

    public static void clean() {
        _parsed.clear();
        _round.set(0);
    }
}
