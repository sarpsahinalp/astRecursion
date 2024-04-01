import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.nio.Attribute;
import org.jgrapht.nio.DefaultAttribute;
import org.jgrapht.nio.dot.DOTExporter;

import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.LinkedHashMap;
import java.util.Map;

public class MethodCallGraph {
    private final Graph<String, DefaultEdge> graph = new DefaultDirectedGraph<>(DefaultEdge.class);

    /**
     * Create a graph from the given CompilationUnit
     * @param cu CompilationUnit to be parsed
     * @throws FileNotFoundException if the file is not found
     */
    public void createGraph(CompilationUnit cu) throws FileNotFoundException {
        cu.accept(new VoidVisitorAdapter<>() {
            @Override
            public void visit(MethodDeclaration md, Object arg) {
                super.visit(md, arg);
                String vertexName = md.resolve().getQualifiedName();
                graph.addVertex(vertexName);
                md.findAll(MethodCallExpr.class).forEach(mce -> {
                    String calleeVertexName = mce.resolve().getQualifiedName();
                    graph.addVertex(calleeVertexName);
                    graph.addEdge(vertexName, calleeVertexName);
                });
            }
        }, null);
    }

    /**
     * Turn the graph into .dot file to visualize
     * @param filePath path to be exported to
     */
    public void exportToDotFile(String filePath) {
        DOTExporter<String, DefaultEdge> exporter = new DOTExporter<>();
        exporter.setVertexAttributeProvider((v) -> {
            Map<String, Attribute> map = new LinkedHashMap<>();
            map.put("label", DefaultAttribute.createAttribute(v));
            return map;
        });

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