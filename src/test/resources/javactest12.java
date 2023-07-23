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
        if (TestPrototype.create.one != null && TestPrototype.create.one.two != TestPrototype.create.one.two.three)
            create().two().three.test();
        else
        	create().two.three().test();

        return TestPrototype.create.one().test;
    }

}
