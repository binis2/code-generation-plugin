package net.binis.codegen.compiler.plugin.parser;

import com.sun.tools.javac.api.JavacTrees;
import com.sun.tools.javac.comp.*;
import com.sun.tools.javac.model.JavacElements;
import com.sun.tools.javac.util.Context;
import net.binis.codegen.tools.Reflection;

public class CodeGenResolve extends Resolve {

    private static final String FIELD = "rs";

    protected CodeGenResolve(Context context) {
        super(context);
        patchCompilerClasses(context);
    }

    private void patchCompilerClasses(Context context) {
        Reflection.setFieldValue(Attr.instance(context), FIELD, this);

        Reflection.setFieldValue(DeferredAttr.instance(context), FIELD, this);
        Reflection.setFieldValue(Check.instance(context), FIELD, this);
        Reflection.setFieldValue(Infer.instance(context), FIELD, this);
        Reflection.setFieldValue(Flow.instance(context), FIELD, this);
        Reflection.setFieldValue(LambdaToMethod.instance(context), FIELD, this);
        Reflection.setFieldValue(Lower.instance(context), FIELD, this);
//        Reflection.setFieldValue(Gen.instance(context), FIELD, this);
//        Reflection.setFieldValue(
//                        ReflectUtil.method(
//                                        ReflectUtil.type("com.sun.tools.javac.jvm.StringConcat"), "instance", Context.class)
//                                .invokeStatic(context), FIELD)
//                .set(this);
        Reflection.setFieldValue(JavacTrees.instance(context), "resolve", this);
        Reflection.setFieldValue(Annotate.instance(context), "resolve", this);
        Reflection.setFieldValue(TransTypes.instance(context), "resolve", this);
        Reflection.setFieldValue(JavacElements.instance(context), "resolve", this);

//        if( JreUtil.isJava11orLater() )
//        {
//            // Allow @var to work with properties.
//            // Note, this is not as scary as it looks. Setting allowLocalVariableTypeInference to false only turns off
//            // unnecessary name checking so we can use @var annotation type, which should be allowed because `@` effectively
//            // escapes the name, so there really isn't any conflict with Java's 'var' construct. Just sayin'
//            ReflectUtil.field( this, "allowLocalVariableTypeInference" ).set( false );
//        }
//        else if( JreUtil.isJava17orLater() )
//        {
//            ReflectUtil.field( ReflectUtil.method( "com.sun.tools.javac.comp.TransPattern", "instance", Context.class )
//                    .invokeStatic( context ), FIELD ).set( this );
//        }        
    }

    public static Resolve instance(Context ctx) {
        Resolve resolve = ctx.get(resolveKey);
        if (!(resolve instanceof CodeGenResolve)) {
            ctx.put(resolveKey, (Resolve) null);
            resolve = new CodeGenResolve(ctx);
        }

        return resolve;
    }

}
