package recursionCheck;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.nodeTypes.NodeWithType;
import org.apiguardian.api.API;
import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.traverse.DepthFirstIterator;

import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.stream.Collectors;

import static recursionCheck.RecursionCheck.getParametersOfMethod;

/**
 * Create a graph of method calls from a CompilationUnit
 */
@API(status = API.Status.INTERNAL)
public class MethodCallGraph {
    private final Graph<String, DefaultEdge> graph;

    private final String[] excludedMethodIdentifiers;

    public MethodCallGraph(Method... excludedMethods) {
        this.graph = new DefaultDirectedGraph<>(DefaultEdge.class);
        this.excludedMethodIdentifiers = new String[excludedMethods.length];
        for (int i = 0; i < excludedMethods.length; i++) {
            Method m = excludedMethods[i];
            this.excludedMethodIdentifiers[i] = m != null ? m.getDeclaringClass().getName() + "." + m.getName() + getParametersOfMethod(m) : null;
        }
    }

    /**
     * Create a graph from the given CompilationUnit
     * @param cu CompilationUnit to be parsed
     */
    public void createGraph(CompilationUnit cu) {
        cu.accept(new VisitorAdapter(graph, excludedMethodIdentifiers), null);
    }

    /**
     * Extract a subgraph from the given graph starting from the given vertex
     * @param startVertex Vertex to start the extraction from
     * @return Subgraph of the given graph
     */
    public Graph<String, DefaultEdge> extractSubgraph(String startVertex) {
        DefaultDirectedGraph<String, DefaultEdge> subgraph = new DefaultDirectedGraph<>(null, graph.getEdgeSupplier(), false);

        // Set to keep track of visited vertices
        Set<String> visited = new HashSet<>();

        // Initialize DepthFirstIterator
        Iterator<String> iterator = new DepthFirstIterator<>(graph, startVertex);

        // Add start vertex to subgraph
        subgraph.addVertex(startVertex);
        visited.add(startVertex);

        // Iterate through the graph
        while (iterator.hasNext()) {
            String vertex = iterator.next();
            // Add vertex to subgraph if not already visited
            if (!visited.contains(vertex)) {
                subgraph.addVertex(vertex);
                visited.add(vertex);
            }
            // Add edges to subgraph
            graph.edgesOf(vertex).forEach(edge -> {
                String source = graph.getEdgeSource(edge);
                String target = graph.getEdgeTarget(edge);
                if (visited.contains(source) && visited.contains(target)) {
                    subgraph.addEdge(source, target);
                }
            });
        }

        return subgraph;
    }

    /**
     * Get the parameter types of a method
     * @param md MethodDeclaration to get the parameter types from
     * @return String of parameter types
     */
    public static String getParameterTypes(MethodDeclaration md) {
        return "(" + md.getParameters().stream().map(NodeWithType::getTypeAsString).collect(Collectors.joining(", ")) + ")";
    }

    /**
     * Get the argument types of a method call
     * @param mce MethodCallExpr to get the argument types from
     * @return String of argument types
     */
    public static String getArgumentTypes(MethodCallExpr mce) {
        return "(" + mce.getArguments().stream().map(argument -> argument.calculateResolvedType().describe()).collect(Collectors.joining(", ")) + ")";
    }

    public Graph<String, DefaultEdge> getGraph() {
        return graph;
    }
}