package org.example.ReflectionRecursion;

import java.lang.reflect.Method;

public class Example {
    public void someMethod() {
        try {
            System.out.println("Hello, World!");
            Method method = getClass().getMethod("someMethod");
            method.invoke(this); // Recursive invocation using reflection
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

