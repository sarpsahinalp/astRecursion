import RecursionCheck.RecursionCheckAssert;
import com.github.javaparser.ParseResult;
import com.github.javaparser.ParserConfiguration;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.symbolsolver.JavaSymbolSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.*;
import com.github.javaparser.utils.SourceRoot;
import org.jgrapht.alg.cycle.CycleDetector;
import org.jgrapht.graph.DefaultEdge;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

public class AstTest {

    @Test
    void renders() throws IOException {
        // Convert ComplexNoRecursionToDot
        MethodCallGraph methodCallGraph = new MethodCallGraph();
        List<Optional<CompilationUnit>> asts = parseFromSourceRoot("/home/sarps/IdeaProjects/astRecursion/src/main/java/org/example");
        try {
            for (Optional<CompilationUnit> ast : asts) {
                if (ast.isPresent()) {
                    methodCallGraph.createGraph(ast.get());
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        methodCallGraph.exportToDotFile("ComplexNoRecursion.dot");
    }

    @Test
    void testDetectSimpleRecursionWithGraph() throws IOException {
        MethodCallGraph methodCallGraph = new MethodCallGraph();
        List<Optional<CompilationUnit>> asts = parseFromSourceRoot("/home/sarps/IdeaProjects/astRecursion/src/main/java/org/example/SimpleRecursion");
        try {
            for (Optional<CompilationUnit> ast : asts) {
                if (ast.isPresent()) {
                    methodCallGraph.createGraph(ast.get());
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        CycleDetector<String, DefaultEdge> detector = new CycleDetector<>(methodCallGraph.getGraph());
        assertThat(detector.detectCycles()).isTrue();
    }

    @Test
    void testDetectRecursionForComplex() throws IOException {
        MethodCallGraph methodCallGraph = new MethodCallGraph();
        List<Optional<CompilationUnit>> asts = parseFromSourceRoot("/home/sarps/IdeaProjects/astRecursion/src/main/java/org/example/MoreComplexRecursion");
        try {
            for (Optional<CompilationUnit> ast : asts) {
                if (ast.isPresent()) {
                    methodCallGraph.createGraph(ast.get());
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        System.err.println(methodCallGraph.getGraph().toString());

        CycleDetector<String, DefaultEdge> detector = new CycleDetector<>(methodCallGraph.getGraph());
        assertThat(detector.detectCycles()).isTrue();
    }

    @Test
    void testDetectNoComplexRecursionAcrossClasses() throws IOException {
        MethodCallGraph methodCallGraph = new MethodCallGraph();

        List<Optional<CompilationUnit>> asts = parseFromSourceRoot("/home/sarps/IdeaProjects/astRecursion/src/main/java/org/example/ComplexNoRecursion");
        try {
            for (Optional<CompilationUnit> ast : asts) {
                if (ast.isPresent()) {
                    methodCallGraph.createGraph(ast.get());
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        System.err.println(methodCallGraph.getGraph().toString());

        CycleDetector<String, DefaultEdge> detector = new CycleDetector<>(methodCallGraph.getGraph());
        assertThat(detector.detectCycles()).isFalse();
    }

    @Test
    void testDetectRecursionDynamicDispatch() throws IOException {
        MethodCallGraph methodCallGraph = new MethodCallGraph();
        List<Optional<CompilationUnit>> asts = parseFromSourceRoot("/home/sarps/IdeaProjects/astRecursion/src/main/java/org/example/DynamicDispatch");
        try {
            for (Optional<CompilationUnit> ast : asts) {
                if (ast.isPresent()) {
                    methodCallGraph.createGraph(ast.get());
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        System.err.println(methodCallGraph.getGraph().toString());

        methodCallGraph.exportToDotFile("DynamicDispatch.dot");

        CycleDetector<String, DefaultEdge> detector = new CycleDetector<>(methodCallGraph.getGraph());
        assertThat(detector.detectCycles()).isTrue();
    }

    @Test
    void testDetectRecursionWithLambdas() throws IOException {
        // TODO find a solution not working, since the method is registered as a lambda
        MethodCallGraph methodCallGraph = new MethodCallGraph();
        List<Optional<CompilationUnit>> asts = parseFromSourceRoot("/home/sarps/IdeaProjects/astRecursion/src/main/java/org/example/Lambdas");
        try {
            for (Optional<CompilationUnit> ast : asts) {
                if (ast.isPresent()) {
                    methodCallGraph.createGraph(ast.get());
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        System.err.println(methodCallGraph.getGraph().toString());

        methodCallGraph.exportToDotFile("Lambdas.dot");

        CycleDetector<String, DefaultEdge> detector = new CycleDetector<>(methodCallGraph.getGraph());
        assertThat(detector.detectCycles()).isTrue();
    }

    @Test
    void testDetectRecursionWithReflection() throws IOException {
        MethodCallGraph methodCallGraph = new MethodCallGraph();
        List<Optional<CompilationUnit>> asts = parseFromSourceRoot("/home/sarps/IdeaProjects/astRecursion/src/main/java/org/example/ReflectionRecursion");
        try {
            for (Optional<CompilationUnit> ast : asts) {
                if (ast.isPresent()) {
                    methodCallGraph.createGraph(ast.get());
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        System.err.println(methodCallGraph.getGraph().toString());

        methodCallGraph.exportToDotFile("ReflectionRecursion.dot");

        CycleDetector<String, DefaultEdge> detector = new CycleDetector<>(methodCallGraph.getGraph());
        assertThat(detector.detectCycles()).isTrue();
    }

    @Test
    void testBenchmarkRecursionCheck() throws IOException {
        long startTime = System.nanoTime();

        MethodCallGraph methodCallGraph = new MethodCallGraph();
        List<Optional<CompilationUnit>> asts = parseFromSourceRoot("/home/sarps/IdeaProjects/astRecursion/src/main/java/org/example/BenchmarkASTRecursion");
        try {
            for (Optional<CompilationUnit> ast : asts) {
                if (ast.isPresent()) {
                    methodCallGraph.createGraph(ast.get());
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        CycleDetector<String, DefaultEdge> detector = new CycleDetector<>(methodCallGraph.getGraph());
        assertThat(detector.detectCycles()).isFalse();

        long endTime = System.nanoTime();
        long duration = (endTime - startTime);
        System.out.println(duration / 1000000000.0 + "s");
    }

    @Test
    void testOverloadedMethodsRecursion() throws IOException {
        MethodCallGraph methodCallGraph = new MethodCallGraph();
        List<Optional<CompilationUnit>> asts = parseFromSourceRoot("/home/sarps/IdeaProjects/astRecursion/src/main/java/org/example/OverloadRecursion");
        try {
            for (Optional<CompilationUnit> ast : asts) {
                if (ast.isPresent()) {
                    methodCallGraph.createGraph(ast.get());
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }


        CycleDetector<String, DefaultEdge> detector = new CycleDetector<>(methodCallGraph.getGraph());
        assertThat(detector.detectCycles()).isTrue();
    }

    @Test
    void testOverloadedMethodsNoRecursion() throws IOException {
        MethodCallGraph methodCallGraph = new MethodCallGraph();
        List<Optional<CompilationUnit>> asts = parseFromSourceRoot("/home/sarps/IdeaProjects/astRecursion/src/main/java/org/example/OverloadedNoRecursion");
        try {
            for (Optional<CompilationUnit> ast : asts) {
                if (ast.isPresent()) {
                    methodCallGraph.createGraph(ast.get());
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        methodCallGraph.exportToDotFile("OverloadedNoRecursion.dot");


        CycleDetector<String, DefaultEdge> detector = new CycleDetector<>(methodCallGraph.getGraph());
        assertThat(detector.detectCycles()).isFalse();
    }

    @Test
    void testOverriddenMethodsRecursion() throws IOException {
        MethodCallGraph methodCallGraph = new MethodCallGraph();
        List<Optional<CompilationUnit>> asts = parseFromSourceRoot("/home/sarps/IdeaProjects/astRecursion/src/main/java/org/example/OverriddenRecursion");
        try {
            for (Optional<CompilationUnit> ast : asts) {
                if (ast.isPresent()) {
                    methodCallGraph.createGraph(ast.get());
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        methodCallGraph.exportToDotFile("OverriddenRecursion.dot");

        CycleDetector<String, DefaultEdge> detector = new CycleDetector<>(methodCallGraph.getGraph());
        assertThat(detector.detectCycles()).isTrue();
    }

    @Test
    void testLambdas()throws IOException {
        RecursionCheckAssert.assertThatSourcesIn(Path.of("/home/sarps/IdeaProjects/astRecursion/src/main/java/org/example/Lambdas"))
                .withLanguageLevel(ParserConfiguration.LanguageLevel.JAVA_17)
                .hasNoRecursion();
    }
    public static List<Optional<CompilationUnit>> parseFromSourceRoot(String pathToSourceRoot) throws IOException {
        CombinedTypeSolver combinedTypeSolver = new CombinedTypeSolver();
        combinedTypeSolver.add(new ReflectionTypeSolver());
        combinedTypeSolver.add(new MemoryTypeSolver());
        combinedTypeSolver.add(new JavaParserTypeSolver(new File(pathToSourceRoot)));
        combinedTypeSolver.add(new ClassLoaderTypeSolver(MethodCallGraph.class.getClassLoader()));

        // Configure JavaParser to use type resolution
        JavaSymbolSolver symbolSolver = new JavaSymbolSolver(combinedTypeSolver);

        SourceRoot sourceRoot = new SourceRoot(Path.of(pathToSourceRoot), new ParserConfiguration().setSymbolResolver(symbolSolver));

        List<ParseResult<CompilationUnit>> parseResults = sourceRoot.tryToParse();

        return parseResults.stream().map(ParseResult::getResult).toList();
    }
}