package org.example.ComplexNoRecursion;

public class Fifth {

    public void callSecond() {
        First first = new First();
        first.noRecursion();
        callThird();
    }

    public void callThird() {
        Third third = new Third();
    }

    public void callFourth() {
        Fourth fourth = new Fourth();
    }
}
