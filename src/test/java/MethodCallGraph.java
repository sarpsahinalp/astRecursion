import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.nio.dot.DOTExporter;

import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;

public class MethodCallGraph {
    private final Graph<String, DefaultEdge> graph = new DefaultDirectedGraph<>(DefaultEdge.class);

    public void createGraph(CompilationUnit cu) throws FileNotFoundException {
        // TODO custom more sophisticated visitor

        cu.accept(new VoidVisitorAdapter<>() {
            @Override
            public void visit(MethodDeclaration md, Object arg) {
                super.visit(md, arg);
                String methodName = md.getNameAsString();
                graph.addVertex(methodName);
                md.findAll(MethodCallExpr.class).forEach(mce -> {
                    String callee = mce.getNameAsString();
                    graph.addVertex(callee);
                    graph.addEdge(methodName, callee);
                });
            }
        }, null);
    }

    public void exportToDotFile(String filePath) {
        DOTExporter<String, DefaultEdge> exporter = new DOTExporter<>();
        try (Writer writer = new FileWriter(filePath)) {
            exporter.exportGraph(graph, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Graph<String, DefaultEdge> getGraph() {
        return graph;
    }
}