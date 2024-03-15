package org.example.ComplexNoRecursionWrong;

public class Third {

    public void callFifth() {
        Fifth fifth = new Fifth();
    }

    public void callFourth() {
        Fourth fourth = new Fourth();
        fourth.callFifth();
    }

    public void callSecond() {
        Second second = new Second();
    }
}
