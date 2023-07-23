package net.binis.codegen.javac.test;

public class TestPrototype {

    public TestPrototype() {
        super();
    }

    public TestPrototype(String string) {
        super();
    }

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

    public static String createNew() {
        return new TestPrototype(TestPrototype.create.one.test()).two.test();
    }

}
