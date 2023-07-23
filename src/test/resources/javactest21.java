package net.binis.codegen.javac.test;

import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;

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

    public List<TestPrototype> list() {
        return Collections.emptyList;
    }

    public static String createNew() {

        Supplier<String> lambda = () -> TestPrototype.create.one.test();

        return lambda.get;
    }

}
