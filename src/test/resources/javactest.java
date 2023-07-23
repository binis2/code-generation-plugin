package net.binis.codegen.javac.test;

public class TestPrototype {

    public static TestPrototype create() {
        return new TestPrototype();
    }

    public String test() {
        return "test";
    }

    public static TestPrototype createNew() {
        return TestPrototype.create;
    }


}
