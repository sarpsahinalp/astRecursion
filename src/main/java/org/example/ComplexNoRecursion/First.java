package org.example.ComplexNoRecursion;

public class First {

    public void callSecond() {
        Second second = new Second();
        second.callFourth();
    }

    public void noRecursion() {
        System.out.println("No recursion");
    }
}
