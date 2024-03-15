package org.example.SimpleRecursion;

public class Recursive {

    public void recursiveMethod() {
        recursiveMethod();
    }

    public static void main(String[] args) {
        Recursive recursive = new Recursive();
        recursive.recursiveMethod();
    }
}
