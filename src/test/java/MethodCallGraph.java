import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.nio.Attribute;
import org.jgrapht.nio.DefaultAttribute;
import org.jgrapht.nio.dot.DOTExporter;

import java.io.*;
import java.util.LinkedHashMap;
import java.util.Map;

public class MethodCallGraph {
    private final Graph<String, DefaultEdge> graph = new DefaultDirectedGraph<>(DefaultEdge.class);

    public void createGraph(CompilationUnit cu) throws FileNotFoundException {
        // TODO custom more sophisticated visitor

        cu.accept(new VoidVisitorAdapter<>() {
            @Override
            public void visit(MethodDeclaration md, Object arg) {
                super.visit(md, arg);
                String className = ((ClassOrInterfaceDeclaration) md.getParentNode().get()).getNameAsString();
                String methodName = md.getNameAsString();
                String vertexName = className + "." + methodName;
                graph.addVertex(vertexName);
                md.findAll(MethodCallExpr.class).forEach(mce -> {
                    String calleeClassName = mce.resolve().getClassName();
                    String callee = mce.getNameAsString();
                    String calleeVertexName = calleeClassName + "." + callee;
                    graph.addVertex(calleeVertexName);
                    graph.addEdge(vertexName, calleeVertexName);
                });
            }
        }, null);
    }

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