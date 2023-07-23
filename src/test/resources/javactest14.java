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

    public static String createNew() {
        return switch (TestPrototype.create.one().test()) {
            case "test" -> TestPrototype.create.one().test();
            default -> create().two.three().test();
        };
    }

}
