package net.binis.codegen.javac.test;

public class TestPrototype {

    public static TestPrototype create() {
        return new TestPrototype();
    }

    public String test() {
        return "test";
    }

    public TestPrototype one() {
        return this;
    }

    public TestPrototype two() {
        return this;
    }

    public TestPrototype three() {
        return this;
    }

    public boolean bool() {
        return false;
    }

    public RuntimeException _throw() {
        return null;
    }

    public static String createNew() {
        if (TestPrototype.create.one.bool()) {
            throw TestPrototype.create.one._throw();
        }
        return TestPrototype.create.one.test();
    }

}
