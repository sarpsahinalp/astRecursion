package org.example.ComplexNoRecursion;

import java.util.Objects;

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
        Object fourth = new Fourth();
    }
}
