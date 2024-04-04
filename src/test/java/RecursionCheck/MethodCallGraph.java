package RecursionCheck;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.nodeTypes.NodeWithType;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleDirectedGraph;

import java.util.stream.Collectors;

public class MethodCallGraph {
    private final Graph<String, DefaultEdge> graph = new SimpleDirectedGraph<>(DefaultEdge.class);

    /**
     * Create a graph from the given CompilationUnit
     * @param cu CompilationUnit to be parsed
     */
    public void createGraph(CompilationUnit cu) {
        cu.accept(new VoidVisitorAdapter<>() {
            @Override
            public void visit(MethodDeclaration md, Object arg) {
                super.visit(md, arg);
                String vertexName = md.resolve().getQualifiedName() + "#" + getParameterTypes(md);
                graph.addVertex(vertexName);
                md.findAll(MethodCallExpr.class).forEach(mce -> {
                    String calleeVertexName = mce.resolve().getQualifiedName() + "#" + getArgumentTypes(mce);
                    graph.addVertex(calleeVertexName);
                    graph.addEdge(vertexName, calleeVertexName);
                });
            }
        }, null);
    }

    private String getParameterTypes(MethodDeclaration md) {
        return md.getParameters().stream().map(NodeWithType::getTypeAsString).collect(Collectors.joining(", "));
    }

    private String getArgumentTypes(MethodCallExpr mce) {
        return mce.getArguments().stream().map(argument -> argument.calculateResolvedType().describe()).collect(Collectors.joining(", "));
    }

    public Graph<String, DefaultEdge> getGraph() {
        return graph;
    }
}