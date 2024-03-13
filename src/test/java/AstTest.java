import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.MethodCallExpr;
import org.jgrapht.alg.cycle.CycleDetector;
import org.jgrapht.graph.DefaultEdge;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.util.List;
import java.util.Objects;
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
    void methodGraph() {
        MethodCallGraph methodCallGraph = new MethodCallGraph();
        List<CompilationUnit> asts = readFromDirectory(Path.of("C:\\Users\\sarps\\IdeaProjects\\astRecursion\\src\\main\\java\\org\\example"));
        try {
            for (CompilationUnit ast : asts) {
                methodCallGraph.createGraph(ast);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        CycleDetector<String, DefaultEdge> detector = new CycleDetector<>(methodCallGraph.getGraph());
        assertThat(detector.detectCycles()).isFalse();
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
}