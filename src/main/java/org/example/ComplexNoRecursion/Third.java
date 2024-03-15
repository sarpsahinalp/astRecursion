package org.example.ComplexNoRecursion;

public class Third {

    public void callFifth() {
        Fifth fifth = new Fifth();
        fifth.callSecond();
    }

    public void callFourth() {
        Fourth fourth = new Fourth();
    }

    public void callSecond() {
        Second second = new Second();
    }
}
