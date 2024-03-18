import com.github.javaparser.ParseResult;
import com.github.javaparser.ParserConfiguration;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.symbolsolver.JavaSymbolSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.ClassLoaderTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.CombinedTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.JavaParserTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.ReflectionTypeSolver;
import com.github.javaparser.utils.SourceRoot;
import org.jgrapht.alg.cycle.CycleDetector;
import org.jgrapht.graph.DefaultEdge;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

public class AstTest {
    private static final PathMatcher JAVAFILEMATCHER = FileSystems.getDefault().getPathMatcher("glob:*.java");

    @Test
    void renders() {
        List<CompilationUnit> asts = readFromDirectory(Path.of("C:\\Users\\sarps\\IdeaProjects\\astRecursion\\src\\main\\java\\org\\example"));
        System.out.println(asts);

        // Return all method declarations from the asts
        for (CompilationUnit ast : asts) {
            List<MethodDeclaration> methodDeclarations = ast.findAll(MethodDeclaration.class);
            for (MethodDeclaration methodDeclaration : methodDeclarations) {
                System.out.println(methodDeclaration.findAll(MethodCallExpr.class));
            }
        }
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
        // TODO Map classes with methods for duplicate method names otherwise it will fail
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

    public static void main(String[] args) throws IOException {
        // Convert ComplexNoRecursionToDot
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

        methodCallGraph.exportToDotFile("ComplexNoRecursion.dot");
    }

    public static List<CompilationUnit> readFromDirectory(Path pathOfDirectory) {
        try (Stream<Path> directoryContentStream = Files.walk(pathOfDirectory)) {
            return directoryContentStream.map(file -> {
                if (!JAVAFILEMATCHER.matches(file.getFileName())) {
                    return null;
                }
                try {
                    return StaticJavaParser.parse(file);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }).filter(Objects::nonNull).toList();
        } catch (IOException e) {
            System.err.println("Error"); //$NON-NLS-1$
        }
        return List.of();
    }

    public static List<Optional<CompilationUnit>> parseFromSourceRoot(String pathToSourceRoot) throws IOException {
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
}