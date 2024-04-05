package RecursionCheck;

import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultEdge;

import static RecursionCheck.MethodCallGraph.getArgumentTypes;
import static RecursionCheck.MethodCallGraph.getParameterTypes;

public class VisitorAdapter extends VoidVisitorAdapter<Object> {
    private final Graph<String, DefaultEdge> graph;

    public VisitorAdapter(Graph<String, DefaultEdge> graph) {
        this.graph = graph;
    }

    @Override
    public void visit(MethodDeclaration md, Object arg) {
        super.visit(md, arg);
        String vertexName = md.resolve().getQualifiedName() + getParameterTypes(md);
        graph.addVertex(vertexName);
        md.findAll(MethodCallExpr.class).forEach(mce -> {
            String calleeVertexName = mce.resolve().getQualifiedName() + getArgumentTypes(mce);
            graph.addVertex(calleeVertexName);
            graph.addEdge(vertexName, calleeVertexName);
        });
    }
}
