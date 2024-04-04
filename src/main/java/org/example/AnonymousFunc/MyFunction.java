package org.example.AnonymousFunc;

interface MyFunction {
    void apply(int n);
}

class RecursiveExample {
    void performOperation(int n, MyFunction function) {
        // Some logic
        function.apply(n);
    }
    public static void main(String[] args) {
        RecursiveExample example = new RecursiveExample();
        example.performOperation(5, new MyFunction() {
            public void apply(int n) {
                if (n > 0) {
                    System.out.println(n);
                    example.performOperation(n - 1, this); // Recursive call
                }
            }
        });

        example.performOperation(5, new MyFunction() {
            public void apply(int n) {
                if (n > 0) {
                    System.out.println(n);
                    example.performOperation(n - 1, this); // Recursive call
                }
            }
        });
    }
}