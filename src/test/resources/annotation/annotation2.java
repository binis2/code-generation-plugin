package net.binis.codegen.javac.test;

public @interface InheritAnnotation extends BaseAnnotation {

    String test2() default "test";

    int ordinal2();

}

