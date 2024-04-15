# AST Recursion Check & Exclude Main Method

## Description

The project contains examples of possible recursion checks and introduces a way to exclude the `main` method itself of any AST checks

## Implementation Exclude Main Method *(does not work for recursion check yet !!!)*

I introduced a new boolean variable in the `UnwantedNodesAssert` class, which is set to false by default. This variable is then passed to `UnwantedNode` in the `hasNo` method of the `UnwantedNodesAssert` class. Subsequently, this variable is propagated down to `JavaFile`, where the Abstract Syntax Tree (AST) is created using JavaParser. At this level, the variable is used to exclude the `main` method from the AST, effectively bypassing every kind of check that is being used currently. A TODO here is to exclude methods that are recursive and only called by the main method.

## Implementation Recursion Check

For recursion checks, we utilize `SourcesRoot` to compile the Abstract Syntax Trees (ASTs) of all the files. Additionally, we maintain solvers to differentiate between classes and resolve class and package references during graph creation. The process involves the following steps:

1. We use JavaParser with the `CombinedTypeSolver`, which combines the `ClassLoaderTypeSolver` and `ReflectionTypeSolver`, to obtain the AST of the files. 
2. Next, we extract the methods from the files using `JavaParser`. 
3. Finally, we parse through these methods to acquire the method call graph. 

Below is an example code snippet demonstrating the new approach for parsing files and obtaining the method call graph:

```java
public static List<Optional<CompilationUnit>> parseFromSourceRoot(String pathToSourceRoot) {
    CombinedTypeSolver combinedTypeSolver = new CombinedTypeSolver();
    combinedTypeSolver.add(new ReflectionTypeSolver());
    combinedTypeSolver.add(new JavaParserTypeSolver(new File(pathToSourceRoot)));
    combinedTypeSolver.add(new ClassLoaderTypeSolver(MethodCallGraph.class.getClassLoader()));

    // Configure JavaParser to use type resolution
    JavaSymbolSolver symbolSolver = new JavaSymbolSolver(combinedTypeSolver);

    SourceRoot sourceRoot = new SourceRoot(Path.of(pathToSourceRoot), new ParserConfiguration().setSymbolResolver(symbolSolver));

    List<ParseResult<CompilationUnit>> parseResults = sourceRoot.tryToParse();

    return parseResults.stream().map(ParseResult::getResult).toList();
}
```

We then use the `MethodCallGraph` class to obtain the method call graph. The `MethodCallGraph` class is responsible for retrieving the method call graph from the files. This graph represents the relationships between methods and the methods they invoke. It is represented using the `JGraphT` library. Here’s an example code snippet demonstrating the new approach for creating the method call graph:

```java
MethodCallGraph methodCallGraph = new MethodCallGraph();
List<Optional<CompilationUnit>> asts = parseFromSourceRoot("Student Source Root Path"); 
try {     
    for (Optional<CompilationUnit> ast : asts) {
        if (ast.isPresent()) {
            methodCallGraph.createGraph(ast.get());
        }
    }
} catch (IOException e) {     
    e.printStackTrace();
}
```

The graph is then used to check for **recursion**. If the graph forms a **cyclic structure**, it indicates that the method is recursive. Here’s an example code snippet demonstrating the new approach for obtaining the method call graph:

```java
CycleDetector<String, DefaultEdge> detector = new CycleDetector<>(methodCallGraph.getGraph());
assertThat(detector.detectCycles()).isFalse();
```

## Edge Cases Recursion Coverage
1. **Mutual Recursion**: Works as expected!
2. **Indirect Recursion**: Works as expected!
3. **Dynamic Dispatch Polymorphism**: Works as expected!
4. **Reflection**: Does not work since the method is called indirectly, possible solution could be to see which method is called through the invoke method??
5. **Complex Graph Structures**: Find some examples!!
6. **Anonymous Inner Classes and Lambdas**: Not working!!!, have to find a more sophisticated way, resolver acknowledges them as Unknown when resolved, therefore doesn't seem to create cycles in the method call graph
7. **Static Methods**: Works as expected!
8. **Overloaded Methods**: After new identifiers, works as expected!
9. **Overridden Methods**: Works as expected!

## Performance

In the directory BenchmarkASTRecursion, there is a shell script which creates 10000 classes with a simple method. This package is then used to measure the time in the test `testBenchmarkRecursionCheck`. The latest result is: **3.928728603s**.

## Ideas for the current problems

Create a fingerprint for the methods which can be identifiable by the method call graph therefore result in always correct method call graphs, for reflection and anonymous methods think of another way to parse them!

## Added features

### 1.Exclude Method

Added exclude method for the recursionCheckAssert, which the instructor can pass in the Methods to be excluded from the MethodCallGraph. This is done by passing methods using Reflection.

### 2.Starting Method

Added a starting method for the recursionCheckAssert, which the instructor can pass in the Method to start the recursion check from. This is done by passing the method using Reflection.