package RecursionCheck;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.nodeTypes.NodeWithType;
import org.apiguardian.api.API;
import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Create a graph of method calls from a CompilationUnit
 */
@API(status = API.Status.INTERNAL)
public class MethodCallGraph {
    private final Graph<String, DefaultEdge> graph;

    public MethodCallGraph () {
        this.graph = new DefaultDirectedGraph<>(DefaultEdge.class);
    }

    /**
     * Create a graph from the given CompilationUnit
     * @param cu CompilationUnit to be parsed
     */
    public void createGraph(CompilationUnit cu) {
        cu.accept(new VisitorAdapter(graph), null);
    }

    /**
     * Extract a subgraph from the given graph starting from the given vertex
     * @param graph Graph to extract the subgraph from
     * @param startVertex Vertex to start the extraction from
     * @return Subgraph of the given graph
     */
    public static Graph<String, DefaultEdge> extractSubgraph(Graph<String, DefaultEdge> graph, String startVertex) {
        DefaultDirectedGraph<String, DefaultEdge> subgraph = new DefaultDirectedGraph<>(null, graph.getEdgeSupplier(), false);

        // Add the starting vertex to the subgraph
        subgraph.addVertex(startVertex);

        // Perform DFS, since we want to add all vertices reachable from the starting vertex
        dfs(graph, startVertex, subgraph, new HashSet<>());

        return subgraph;
    }

    /**
     * Perform a depth-first search on the graph starting from the given vertex
     * @param graph Graph to perform the search on
     * @param vertex Vertex to start the search from
     * @param subgraph Subgraph to add the vertices to
     * @param visited Set of visited vertices
     */
    private static void dfs(Graph<String, DefaultEdge> graph, String vertex, Graph<String, DefaultEdge> subgraph, Set<Object> visited) {
        visited.add(vertex);

        for (DefaultEdge edge : graph.outgoingEdgesOf(vertex)) {
            String targetVertex = graph.getEdgeTarget(edge);
            subgraph.addVertex(targetVertex);

            // Add the edge to the subgraph
            subgraph.addEdge(vertex, targetVertex);

            // Add the target vertex to the subgraph if it has not been visited yet
            if (!visited.contains(targetVertex)) {
                subgraph.addVertex(targetVertex);
                dfs(graph, targetVertex, subgraph, visited);
            }
        }
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