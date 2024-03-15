package org.example.ComplexNoRecursion;

public class First {

    public void callSecond() {
        Second second = new Second();
        second.callFourth();
    }

    public void noRecursion() {
        System.out.println("No recursion");
    }

    public static void main(String[] args) {
        First first = new First();
        first.callSecond();
    }
}
