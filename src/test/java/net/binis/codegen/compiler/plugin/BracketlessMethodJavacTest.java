package net.binis.codegen.compiler.plugin;

import net.binis.codegen.test.BaseCodeGenCompilerTest;
import net.binis.codegen.tools.Reflection;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class BracketlessMethodJavacTest extends BaseCodeGenCompilerTest {

    @Test
    void testImmediateReturn() {
        var cls = testSingle("javactest.java", "net.binis.codegen.javac.test.TestPrototype");
        assertNotNull(cls);
        var inst = Reflection.invokeStatic("createNew", cls);
        assertNotNull(inst);
        assertEquals("test", Reflection.invoke("test", inst));
    }

    @Test
    void testImmediateReturnWithoutNamespace() {
        var cls = testSingle("javactest11.java", "net.binis.codegen.javac.test.TestPrototype");
        assertNotNull(cls);
        var inst = Reflection.invokeStatic("createNew", cls);
        assertNotNull(inst);
        assertEquals("test", Reflection.invoke("test", inst));
    }


    @Test
    void testAlterToMethodReturn() {
        var cls = testSingle("javactest2.java", "net.binis.codegen.javac.test.TestPrototype");
        assertNotNull(cls);
        assertEquals("test", Reflection.invokeStatic("createNew", cls));
    }

    @Test
    void testAlterToAlterReturn() {
        var cls = testSingle("javactest3.java", "net.binis.codegen.javac.test.TestPrototype");
        assertNotNull(cls);
        assertEquals("test", Reflection.invokeStatic("createNew", cls));
    }

    @Test
    void testLongChainReturn() {
        var cls = testSingle("javactest4.java", "net.binis.codegen.javac.test.TestPrototype");
        assertNotNull(cls);
        assertEquals("test", Reflection.invokeStatic("createNew", cls));
    }

    @Test
    void testAssignment() {
        var cls = testSingle("javactest5.java", "net.binis.codegen.javac.test.TestPrototype");
        assertNotNull(cls);
        assertEquals("test", Reflection.invokeStatic("createNew", cls));
    }

    @Test
    void testVarAssignment() {
        var cls = testSingle("javactest6.java", "net.binis.codegen.javac.test.TestPrototype");
        assertNotNull(cls);
        assertEquals("test", Reflection.invokeStatic("createNew", cls));
    }

    @Test
    void testVarAssignmentMixed() {
        var cls = testSingle("javactest7.java", "net.binis.codegen.javac.test.TestPrototype");
        assertNotNull(cls);
        assertEquals("test", Reflection.invokeStatic("createNew", cls));
    }

    @Test
    void testVarAssignmentMixed2() {
        var cls = testSingle("javactest8.java", "net.binis.codegen.javac.test.TestPrototype");
        assertNotNull(cls);
        assertEquals("test", Reflection.invokeStatic("createNew", cls));
    }

    @Test
    void testVarAssignmentSplit() {
        var cls = testSingle("javactest9.java", "net.binis.codegen.javac.test.TestPrototype");
        assertNotNull(cls);
        assertEquals("test", Reflection.invokeStatic("createNew", cls));
    }

    @Test
    void testIfWithoutBlocks() {
        var cls = testSingle("javactest10.java", "net.binis.codegen.javac.test.TestPrototype");
        assertNotNull(cls);
        assertEquals("test", Reflection.invokeStatic("createNew", cls));
    }

    @Test
    void testIfWithoutBlocks2() {
        var cls = testSingle("javactest12.java", "net.binis.codegen.javac.test.TestPrototype");
        assertNotNull(cls);
        assertEquals("test", Reflection.invokeStatic("createNew", cls));
    }

    @Test
    void testTryCatch() {
        var cls = testSingle("javactest13.java", "net.binis.codegen.javac.test.TestPrototype");
        assertNotNull(cls);
        assertEquals("test", Reflection.invokeStatic("createNew", cls));
    }

    @Test
    void testExpressionSwitch() {
        var cls = testSingle("javactest14.java", "net.binis.codegen.javac.test.TestPrototype");
        assertNotNull(cls);
        assertEquals("test", Reflection.invokeStatic("createNew", cls));
    }

    @Test
    void testSwitch() {
        var cls = testSingle("javactest15.java", "net.binis.codegen.javac.test.TestPrototype");
        assertNotNull(cls);
        assertEquals("test", Reflection.invokeStatic("createNew", cls));
    }


    @Test
    void testDoWhile() {
        var cls = testSingle("javactest16.java", "net.binis.codegen.javac.test.TestPrototype");
        assertNotNull(cls);
        assertEquals("test", Reflection.invokeStatic("createNew", cls));
    }

    @Test
    void testDoWhileWithCond() {
        var cls = testSingle("javactest17.java", "net.binis.codegen.javac.test.TestPrototype");
        assertNotNull(cls);
        assertEquals("test", Reflection.invokeStatic("createNew", cls));
    }


    @Test
    void testWhile() {
        var cls = testSingle("javactest18.java", "net.binis.codegen.javac.test.TestPrototype");
        assertNotNull(cls);
        assertEquals("test", Reflection.invokeStatic("createNew", cls));
    }

    @Test
    void testFor() {
        var cls = testSingle("javactest19.java", "net.binis.codegen.javac.test.TestPrototype");
        assertNotNull(cls);
        assertEquals("test", Reflection.invokeStatic("createNew", cls));
    }

    @Test
    void testEnhancedFor() {
        var cls = testSingle("javactest20.java", "net.binis.codegen.javac.test.TestPrototype");
        assertNotNull(cls);
        assertEquals("test", Reflection.invokeStatic("createNew", cls));
    }

    @Test
    void testLambda() {
        var cls = testSingle("javactest21.java", "net.binis.codegen.javac.test.TestPrototype");
        assertNotNull(cls);
        assertEquals("test", Reflection.invokeStatic("createNew", cls));
    }

    @Test
    void testSynchronized() {
        var cls = testSingle("javactest22.java", "net.binis.codegen.javac.test.TestPrototype");
        assertNotNull(cls);
        assertEquals("test", Reflection.invokeStatic("createNew", cls));
    }

    @Test
    void testConditional() {
        var cls = testSingle("javactest23.java", "net.binis.codegen.javac.test.TestPrototype");
        assertNotNull(cls);
        assertEquals("test", Reflection.invokeStatic("createNew", cls));
    }

    @Test
    void testThrow() {
        var cls = testSingle("javactest24.java", "net.binis.codegen.javac.test.TestPrototype");
        assertNotNull(cls);
        assertEquals("test", Reflection.invokeStatic("createNew", cls));
    }

    @Test
    void testAssert() {
        var cls = testSingle("javactest25.java", "net.binis.codegen.javac.test.TestPrototype");
        assertNotNull(cls);
        assertEquals("test", Reflection.invokeStatic("createNew", cls));
    }

    @Test
    void testInstanceOf() {
        var cls = testSingle("javactest26.java", "net.binis.codegen.javac.test.TestPrototype");
        assertNotNull(cls);
        assertEquals("test", Reflection.invokeStatic("createNew", cls));
    }

    @Test
    void testCatch() {
        var cls = testSingle("javactest27.java", "net.binis.codegen.javac.test.TestPrototype");
        assertNotNull(cls);
        assertEquals("test", Reflection.invokeStatic("createNew", cls));
    }

    @Test
    void testAssignOp() {
        var cls = testSingle("javactest28.java", "net.binis.codegen.javac.test.TestPrototype");
        assertNotNull(cls);
        assertEquals("test", Reflection.invokeStatic("createNew", cls));
    }

    @Test
    void testUnary() {
        var cls = testSingle("javactest29.java", "net.binis.codegen.javac.test.TestPrototype");
        assertNotNull(cls);
        assertEquals("test", Reflection.invokeStatic("createNew", cls));
    }

    @Test
    void testTypeCast() {
        var cls = testSingle("javactest30.java", "net.binis.codegen.javac.test.TestPrototype");
        assertNotNull(cls);
        assertEquals("test", Reflection.invokeStatic("createNew", cls));
    }

    @Test
    void testNewClass() {
        var cls = testSingle("javactest31.java", "net.binis.codegen.javac.test.TestPrototype");
        assertNotNull(cls);
        assertEquals("test", Reflection.invokeStatic("createNew", cls));
    }

    @Test
    void testNewArray() {
        var cls = testSingle("javactest32.java", "net.binis.codegen.javac.test.TestPrototype");
        assertNotNull(cls);
        assertEquals("test", Reflection.invokeStatic("createNew", cls));
    }

}
