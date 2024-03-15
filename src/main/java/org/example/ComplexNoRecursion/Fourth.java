package org.example.ComplexNoRecursion;

public class Fourth {

    public void callFifth() {
        Fifth fifth = new Fifth();
        fifth.callSecond();
    }

    public void callThird() {
        Third third = new Third();
        third.callFifth();
    }

    public void callSecond() {
        Second second = new Second();
        second.callThird();
    }
}
