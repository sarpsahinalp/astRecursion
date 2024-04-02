import com.github.javaparser.ast.CompilationUnit;
import org.jgrapht.alg.cycle.CycleDetector;
import org.jgrapht.graph.DefaultEdge;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

public class CheckRecursionWithPartialGraphTest {

    // TODO enable tests to determine which methods are to be considered for the recursion check

    @Test
    void javafxTest() throws IOException {
        MethodCallGraph methodCallGraph = new MethodCallGraph();
        List<Optional<CompilationUnit>> asts = AstTest.parseFromSourceRoot("/home/sarps/IdeaProjects/astRecursion/src/main/java/org/example/JavaFxStudent");
        try {
            for (Optional<CompilationUnit> ast : asts) {
                if (ast.isPresent()) {
                    methodCallGraph.createGraph(ast.get());
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        methodCallGraph.exportToDotFile("JavaFxStudent.dot");
    }
}
