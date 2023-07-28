package net.binis.codegen.javac.test;

public @interface BaseAnnotation {

    String test() default "test";

    int ordinal();

}
