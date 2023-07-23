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
        try {
            TestPrototype.create.one();
        } catch (RuntimeException e) {
            create().two.three().test();
        } catch (Exception e) {
            create().two().three.test();
        }

        return TestPrototype.create.one().test;
    }

}
