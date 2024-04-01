package org.example.ExcludeMain;

public class Student {

    public static void method() {
        method();
    }

    public static void main(String[] args) {
        for (int i = 0; i < 10; i++) {
            System.out.println(i);
        }
        method();
    }
}
