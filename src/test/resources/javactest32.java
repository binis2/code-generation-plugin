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

    public int dim() {
        return 10;
    }

    public static String createNew() {
        var arr = new int[TestPrototype.create.one.dim()];
        arr = new int[]{TestPrototype.create.one.dim()};
        return TestPrototype.create.one.test();
    }

}
