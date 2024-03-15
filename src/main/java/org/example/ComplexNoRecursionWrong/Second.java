package org.example.ComplexNoRecursionWrong;

public class Second {

    public void callThird() {
        Third third = new Third();
    }

    public void callFourth() {
        Fourth fourth = new Fourth();
        fourth.callThird();
    }

    public void callFifth() {
        Fifth fifth = new Fifth();
    }
}
