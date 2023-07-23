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

    public int value() {
        return 10;
    }

    public boolean bool() {
        return false;
    }

    public static String createNew() {
        var result = -TestPrototype.create.one.two.value();
        if (!TestPrototype.create.one.bool()) {
            return TestPrototype.create.test();
        }
        return TestPrototype.create.one.two.three.test();
    }

}
