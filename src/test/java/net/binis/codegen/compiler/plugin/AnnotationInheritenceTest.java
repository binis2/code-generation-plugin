package net.binis.codegen.compiler.plugin;

import net.binis.codegen.test.BaseCodeGenCompilerTest;
import org.junit.jupiter.api.Test;

import static net.binis.codegen.tools.Reflection.findMethod;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class AnnotationInheritenceTest extends BaseCodeGenCompilerTest {

    @Test
    void test() {
        var loader = testMulti("annotation/annotation1.java", "annotation/annotation2.java", "annotation/annotation3.java");
        var cls = loader.findClass("net.binis.codegen.javac.test.InheritAnnotation");
        assertNotNull(cls);
        assertNotNull(findMethod("test", cls));
        assertNotNull(findMethod("ordinal", cls));
//        assertEquals("test", Reflection.invoke("test", inst));
    }

}
