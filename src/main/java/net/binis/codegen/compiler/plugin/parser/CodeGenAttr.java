package net.binis.codegen.compiler.plugin.parser;

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

import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.code.Symtab;
import com.sun.tools.javac.code.Type;
import com.sun.tools.javac.comp.*;
import com.sun.tools.javac.main.JavaCompiler;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.TreeMaker;
import com.sun.tools.javac.tree.TreeScanner;
import com.sun.tools.javac.util.List;
import com.sun.tools.javac.util.*;
import net.binis.codegen.tools.Reflection;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;
import java.util.function.Predicate;

import static java.util.Objects.nonNull;
import static net.binis.codegen.tools.Reflection.*;
import static net.binis.codegen.tools.Tools.in;

public class CodeGenAttr extends Attr {
    protected final Stack<JCTree.JCFieldAccess> _selects;
    protected final Stack<JCTree> _stack = new Stack<>();
    protected final Map<JCTree, JCTree.JCMethodInvocation> _rewritten = new HashMap<>();

    protected static final Method TYPE_ENVS_GET = findMethod("get", loadClass("com.sun.tools.javac.comp.TypeEnvs"), Symbol.TypeSymbol.class);
    protected static final Field TYPE_ENVS_FIELD = findField(Attr.class, "typeEnvs");
    protected static final Field MAKER_FIELD = findField(Attr.class, "make");
    protected static final Field ENV_FIELD = findField(Attr.class, "env");

    public static CodeGenAttr instance(Context ctx) {
        Attr attr = ctx.get(attrKey);
        if (!(attr instanceof CodeGenAttr)) {
            ctx.put(attrKey, (Attr) null);
            attr = new CodeGenAttr(ctx);
        }

        return (CodeGenAttr) attr;
    }

    protected CodeGenAttr(Context context) {
        super(context);
        _selects = new Stack<>();
        patchCompilerClasses(context);
    }

    protected void patchCompilerClasses(Context context) {
        setFieldValue(Resolve.instance(context), "attr", this);
        setFieldValue(ArgumentAttr.instance(context), "attr", this);
        setFieldValue(DeferredAttr.instance(context), "attr", this);
//                MemberEnter.instance(ctx),
//                Lower.instance(ctx),
//                TransTypes.instance(ctx),
//                Annotate.instance(ctx),
//                TypeAnnotations.instance(ctx),
//                JavacTrees.instance(ctx),
        setFieldValue(JavaCompiler.instance(context), "attr", this);
    }

    @Override
    public Type attribType(JCTree tree, Env<AttrContext> env) {
        return super.attribType(tree, env);
    }


    @Override
    public void visitSelect(JCTree.JCFieldAccess tree) {
        _selects.push(tree);
        try {
            var deferredAttrDiagHandler = suppressDiagnostics(tree);
            try {
                super.visitSelect(tree);
                var diag = deferredAttrDiagHandler.getDiagnostics();
                if (!diag.isEmpty()) {
                    var err = diag.size();
                    if (in(diag.peek().getCode(), "compiler.err.cant.deref", "compiler.err.cant.resolve.location")) {
                        if (tryToRewriteFieldAccess(tree, diag)) {
                            fixDiag(diag, err);
                        }
                    } else if (in(diag.peek().getCode(), "compiler.err.doesnt.exist")) {
                        if (tree.selected instanceof JCTree.JCFieldAccess access) {
                            if (tryRewritePackage(tree)) {
                                fixDiag(diag, err);
                            }
                        } else if (tree.selected instanceof JCTree.JCIdent ident && tryToRewriteIdent(ident) && tryToRewriteFieldAccess(tree, diag)) {
                            fixDiag(diag, err);
                        }
                    }
                }
                //
            } finally {
                restoreDiagnostics(tree, deferredAttrDiagHandler);
            }
        } finally {
            _selects.pop();
            if (_selects.isEmpty()) {
                _rewritten.clear();
            }
        }
    }

    protected void fixDiag(Queue<JCDiagnostic> diag, int err) {
        for (var i = 0; i < err; i++) {
            diag.poll();
        }
    }

    public void visitIdent(JCTree.JCIdent tree) {
        var deferredAttrDiagHandler = suppressDiagnostics(tree);
        try {
            super.visitIdent(tree);
            var diag = deferredAttrDiagHandler.getDiagnostics();
            if (!diag.isEmpty() && in(diag.peek().getCode(), "compiler.err.cant.resolve.location")) {
                if (tryToRewriteIdent(tree)) {
                    diag.clear();
                }
            }
            //
        } finally {
            restoreDiagnostics(tree, deferredAttrDiagHandler);
        }
    }

    protected JCTree.JCMethodInvocation buildMethod(JCTree.JCExpression tree, Env env) {
        var mt = new Type.MethodType(List.nil(), Type.noType, List.nil(), ((Symtab) Reflection.getFieldValue(this, "syms")).methodClass);

        var ri = Reflection.getFieldValue(this, "resultInfo");

        setFieldValue(ri, "pt", mt);
        var exp = getMaker().Apply(List.nil(), tree, List.nil());
        env.tree = exp;
        return exp;
    }

    @SuppressWarnings("unchecked")
    protected boolean tryRewritePackage(JCTree.JCFieldAccess tree) {
        try {
            var maker = getMaker();
            var chain = tryRewritePackage(tree, null);
            chain = maker.Apply(List.nil(), maker.Select(chain, tree.name), List.nil());
            var env = getEnv();
            var type = attribExpr(chain, env);
            if (!type.isErroneous()) {
                env.tree = chain;
                tree.setType(type);
                return replaceTree(env.enclMethod.body, tree, (JCTree.JCMethodInvocation) chain);
            }
        } catch (
                Exception e) {
        }
        return false;
    }

    protected JCTree.JCExpression tryRewritePackage(JCTree.JCFieldAccess tree, JCTree.JCExpression chain) {
        try {
            if (tree.selected instanceof JCTree.JCFieldAccess access) {
                var maker = getMaker();
                chain = tryRewritePackage(access, chain);
                return maker.Apply(List.nil(), maker.Select(chain, access.name), List.nil());
            } else {
                var env = getEnv();
                return buildMethod(tree.selected, env);
            }
        } catch (Exception e) {
        }
        return null;
    }

    protected boolean tryToRewriteFieldAccess(JCTree.JCFieldAccess tree, Queue<JCDiagnostic> diag) {
        try {
            var env = getEnv();
            var exp = buildMethod(tree, env);
            tree.sym = null;
            var prv = diag.size();
            super.visitSelect(tree);
            if (diag.size() == prv) {
                return replaceTree(env.enclMethod.body, tree, exp);
            } else {
                diag.poll();
            }
        } catch (Exception e) {
        } finally {
            _stack.clear();
        }
        return false;
    }

    @SuppressWarnings("unchecked")
    protected boolean tryToRewriteIdent(JCTree.JCIdent tree) {
        try {
            var env = getEnv();
            var exp = buildMethod(tree, env);
            tree.sym = null;
            var type = attribExpr(exp, env);
            if (!type.isErroneous()) {
                tree.setType(type);
                return replaceTree(env.enclMethod.body, tree, exp);
            }
        } catch (Exception e) {
        }
        return false;
    }


    protected boolean replaceTree(JCTree old, JCTree tree, JCTree.JCMethodInvocation exp) {
        //if type discovery is needed use attribExpr(exp, env)
        _stack.push(old);
        try {
            if (old instanceof JCTree.JCBlock block) {
                for (var s : block.getStatements()) {
                    if (replaceTree(s, tree, exp)) {
                        return true;
                    }
                }
                return false;
            } else if (old instanceof JCTree.JCReturn ret) {
                var result = replaceTree(ret.expr, tree, exp);

                if (ret.expr == tree) {
                    exp.type = ret.expr.type;
                    ret.expr = exp;
                    return true;
                } else {
                    return result;
                }
            } else if (old instanceof JCTree.JCMethodInvocation invocation) {
                if (invocation.meth instanceof JCTree.JCFieldAccess meth) {
                    if (meth.selected == tree) {
                        meth.selected = exp;
                        _rewritten.put(tree, exp);
                        return true;
                    } else {
                        if (_rewritten.containsValue(meth.selected)) {
                            return true;
                        }
                        return replaceTree(meth.selected, tree, exp);
                    }
                }
                return false;
            } else if (old instanceof JCTree.JCFieldAccess access) {
                if (access.selected == tree) {
                    access.selected = exp;
                    _rewritten.put(tree, exp);
                    return true;
                } else if (access.selected instanceof JCTree.JCFieldAccess field) {
                    if (_rewritten.containsKey(field)) {
                        field.selected = _rewritten.get(field);
                        return true;
                    }
                    return replaceTree(field, tree, exp);
                } else {
                    return replaceTree(access.selected, tree, exp);
                }
            } else if (old instanceof JCTree.JCVariableDecl variable) {
                if (variable.init == tree) {
                    if (nonNull(variable.vartype)) {
                        exp.type = variable.vartype.type;
                    } else if (nonNull(variable.type)) {
                        exp.type = variable.type;
                    } else if (variable.init instanceof JCTree.JCFieldAccess access && nonNull(access.type)) {
                        exp.type = access.type.getReturnType();
                    }

                    variable.init = exp;
                    return true;
                }
                return replaceTree(variable.init, tree, exp);
            } else if (old instanceof JCTree.JCIf _if) {
                return replaceTree(_if.cond, tree, exp) ||
                        replaceTree(_if.thenpart, tree, exp) ||
                        replaceTree(_if.elsepart, tree, exp);
            } else if (old instanceof JCTree.JCParens parens) {
                var result = replaceTree(parens.expr, tree, exp);
                if (parens.expr == tree) {
                    exp.type = parens.expr.type.getReturnType();
                    parens.expr = exp;
                    return true;
                }
                return result;
            } else if (old instanceof JCTree.JCBinary binary) {
                if (binary.lhs == tree) {
                    exp.type = binary.lhs.type.getReturnType();
                    binary.lhs = exp;
                    setFieldValue(this, "result", exp.type);
                    return true;
                } else if (binary.rhs == tree) {
                    exp.type = binary.rhs.type.getReturnType();
                    binary.rhs = exp;
                    setFieldValue(this, "result", exp.type);
                    return true;
                }
                return replaceTree(binary.lhs, tree, exp) ||
                        replaceTree(binary.rhs, tree, exp);
            } else if (old instanceof JCTree.JCExpressionStatement statement) {
                return replaceTree(statement.expr, tree, exp);
            } else if (old instanceof JCTree.JCTry _try) {
                var result = replaceTree(_try.body, tree, exp) ||
                        replaceTree(_try.finalizer, tree, exp);
                for (var c : _try.catchers) {
                    result |= replaceTree(c, tree, exp);
                }
                for (var c : _try.resources) {
                    result |= replaceTree(c, tree, exp);
                }

                return result;
            } else if (old instanceof JCTree.JCSwitchExpression _switch) {
                var result = replaceTree(_switch.selector, tree, exp);
                for (var c : _switch.cases) {
                    result |= replaceTree(c, tree, exp);
                }
                return result;
            } else if (old instanceof JCTree.JCSwitch _switch) {
                var result = replaceTree(_switch.selector, tree, exp);
                for (var c : _switch.cases) {
                    result |= replaceTree(c, tree, exp);
                }
                return result;
            } else if (old instanceof JCTree.JCCase _case) {
                var result = replaceTree(_case.body, tree, exp);
                for (var c : _case.stats) {
                    result |= replaceTree(c, tree, exp);
                }
                return result;
            } else if (old instanceof JCTree.JCYield _yield) {
                return replaceTree(_yield.value, tree, exp);
            } else if (old instanceof JCTree.JCAssign assign) {
                return replaceTree(assign.lhs, tree, exp) ||
                        replaceTree(assign.rhs, tree, exp);
            } else if (old instanceof JCTree.JCAssignOp assign) {
                return replaceTree(assign.lhs, tree, exp) ||
                        replaceTree(assign.rhs, tree, exp);
            } else if (old instanceof JCTree.JCDoWhileLoop loop) {
                return replaceTree(loop.body, tree, exp) ||
                        replaceTree(loop.cond, tree, exp);
            } else if (old instanceof JCTree.JCWhileLoop loop) {
                return replaceTree(loop.body, tree, exp) ||
                        replaceTree(loop.cond, tree, exp);
            } else if (old instanceof JCTree.JCForLoop loop) {
                var result = replaceTree(loop.body, tree, exp) ||
                        replaceTree(loop.cond, tree, exp);
                for (var c : loop.init) {
                    result |= replaceTree(c, tree, exp);
                }
                for (var c : loop.step) {
                    result |= replaceTree(c, tree, exp);
                }
                return result;
            } else if (old instanceof JCTree.JCEnhancedForLoop loop) {
                return replaceTree(loop.body, tree, exp) ||
                        replaceTree(loop.expr, tree, exp);
            } else if (old instanceof JCTree.JCLambda lambda) {
                return replaceTree(lambda.body, tree, exp);
            } else if (old instanceof JCTree.JCSynchronized sync) {
                return replaceTree(sync.body, tree, exp) ||
                        replaceTree(sync.lock, tree, exp);
            } else if (old instanceof JCTree.JCConditional conditional) {
                return replaceTree(conditional.cond, tree, exp) ||
                        replaceTree(conditional.truepart, tree, exp) ||
                        replaceTree(conditional.falsepart, tree, exp);
            } else if (old instanceof JCTree.JCThrow _throw) {
                return replaceTree(_throw.expr, tree, exp);
            } else if (old instanceof JCTree.JCAssert _assert) {
                return replaceTree(_assert.cond, tree, exp) ||
                        replaceTree(_assert.detail, tree, exp);
            } else if (old instanceof JCTree.JCInstanceOf inst) {
                return replaceTree(inst.expr, tree, exp);
            } else if (old instanceof JCTree.JCCatch _catch) {
                return replaceTree(_catch.body, tree, exp);
            } else if (old instanceof JCTree.JCUnary unary) {
                return replaceTree(unary.arg, tree, exp);
            } else if (old instanceof JCTree.JCTypeCast cast) {
                return replaceTree(cast.expr, tree, exp);
            } else if (old instanceof JCTree.JCNewClass newClass) {
                var result = false;
                for (var c : newClass.args) {
                    result |= replaceTree(c, tree, exp);
                }
                return result;
            } else if (old instanceof JCTree.JCNewArray newArray) {
                var result = false;
                for (var c : newArray.dims) {
                    result |= replaceTree(c, tree, exp);
                }
                if (nonNull(newArray.elems)) {
                    for (var c : newArray.elems) {
                        result |= replaceTree(c, tree, exp);
                    }
                }
                return result;
            } else if (old instanceof JCTree.JCIdent ident) {
                return false;
            }
            return false;
        } finally {
            _stack.pop();
        }
    }

    public void visitVarDef(JCTree.JCVariableDecl tree) {
        super.visitVarDef(tree);
        tree.pos();
    }

    protected Log getLogger() {
        return Reflection.getFieldValue(this, "log");
    }

    protected DeferredAttrDiagHandler suppressDiagnostics(JCTree tree) {
        return new DeferredAttrDiagHandler(getLogger(), tree);
    }

    protected void restoreDiagnostics(JCTree tree, DeferredAttrDiagHandler deferredAttrDiagHandler) {
        Queue<JCDiagnostic> diagnostics = deferredAttrDiagHandler.getDiagnostics();
        if (!diagnostics.isEmpty()) {
            deferredAttrDiagHandler.reportDeferredDiagnostics();
        }
        getLogger().popDiagnosticHandler(deferredAttrDiagHandler);
    }

    @Override
    public void attribClass(JCDiagnostic.DiagnosticPosition pos, Symbol.ClassSymbol c) {
        var env = (Env) invoke(TYPE_ENVS_GET, getFieldValue(TYPE_ENVS_FIELD, this), c);
        var deferredAttrDiagHandler = suppressDiagnostics(env.tree);
        try {
            super.attribClass(pos, c);
            var diag = deferredAttrDiagHandler.getDiagnostics();
            if (!diag.isEmpty()) {
                if ("compiler.err.cant.extend.intf.annotation".equals(diag.peek().getCode()) &&
                        handleInheritedAnnotations((JCTree.JCClassDecl) env.tree)) {
                    diag.poll();
                }
            }
        } finally {
            restoreDiagnostics(env.tree, deferredAttrDiagHandler);
        }
    }

    protected boolean handleInheritedAnnotations(JCTree.JCClassDecl tree) {
        var result = false;
        for (var inh : tree.implementing) {
            result |= inheritAnnotation(tree, inh);
        }
        return result;
    }

    protected boolean inheritAnnotation(JCTree.JCClassDecl tree, JCTree.JCExpression inh) {
        var obj = getFieldValue(inh, "sym");
        var members = tree.getMembers().stream().filter(JCTree.JCMethodDecl.class::isInstance).map(JCTree.JCMethodDecl.class::cast).map(JCTree.JCMethodDecl::getName).map(Name::toString).toList();
        if (obj instanceof Symbol.ClassSymbol sym && sym.isAnnotationType()) {
            for (var member : sym.members().getSymbols()) {
                if (member instanceof Symbol.MethodSymbol method) {
                    var name = method.getSimpleName().toString();
                    if (members.stream().noneMatch(name::equals)) {
                        var mtd = getMaker().MethodDef(method, null);
                        tree.sym.members().enter(method);
                        tree.defs.append(mtd);
                    }
                }
            }
            return true;
        }
        return false;
    }

    protected Env getEnv() {
        return getFieldValue(ENV_FIELD, this);
    }

    protected TreeMaker getMaker() {
        return getFieldValue(MAKER_FIELD, this);
    }

    class DeferredDiagnosticHandler extends Log.DiagnosticHandler {
        protected Queue<JCDiagnostic> deferred = new ListBuffer<>();
        protected final Predicate<JCDiagnostic> filter;

        public DeferredDiagnosticHandler(Log log) {
            this(log, null);
        }

        public DeferredDiagnosticHandler(Log log, Predicate<JCDiagnostic> filter) {
            this.filter = filter;
            install(log);
        }

        @Override
        public void report(JCDiagnostic diag) {
            if (!diag.isFlagSet(JCDiagnostic.DiagnosticFlag.NON_DEFERRABLE) &&
                    (filter == null || filter.test(diag))) {
                deferred.add(diag);
            } else {
                prev.report(diag);
            }
        }

        public Queue<JCDiagnostic> getDiagnostics() {
            return deferred;
        }

        /**
         * Report all deferred diagnostics.
         */
        public void reportDeferredDiagnostics() {
            reportDeferredDiagnostics(EnumSet.allOf(JCDiagnostic.Kind.class));
        }

        /**
         * Report selected deferred diagnostics.
         */
        public void reportDeferredDiagnostics(Set<JCDiagnostic.Kind> kinds) {
            JCDiagnostic d;
            while ((d = deferred.poll()) != null) {
                if (kinds.contains(d.getKind()))
                    prev.report(d);
            }
            deferred = null; // prevent accidental ongoing use
        }
    }

    class DeferredAttrDiagHandler extends DeferredDiagnosticHandler {
        static class PosScanner extends TreeScanner {
            JCDiagnostic.DiagnosticPosition pos;
            boolean found = false;

            PosScanner(JCDiagnostic.DiagnosticPosition pos) {
                this.pos = pos;
            }

            @Override
            public void scan(JCTree tree) {
                if (tree != null &&
                        tree.pos() == pos) {
                    found = true;
                }
                super.scan(tree);
            }
        }

        DeferredAttrDiagHandler(Log log, JCTree newTree) {
            super(log, d -> {
                DeferredAttrDiagHandler.PosScanner posScanner = new DeferredAttrDiagHandler.PosScanner(d.getDiagnosticPosition());
                posScanner.scan(newTree);
                return posScanner.found;
            });
        }
    }
}
