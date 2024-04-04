package RecursionCheck;

import com.github.javaparser.ParseResult;
import com.github.javaparser.ParserConfiguration;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.symbolsolver.JavaSymbolSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.ClassLoaderTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.CombinedTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.JavaParserTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.ReflectionTypeSolver;
import com.github.javaparser.utils.SourceRoot;
import org.jgrapht.Graph;
import org.jgrapht.alg.cycle.CycleDetector;
import org.jgrapht.graph.DefaultEdge;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;

public class RecursionCheck {

    private static final Logger LOG = LoggerFactory.getLogger(RecursionCheck.class);

    /**
     * Check if the startingNode has a recursive call
     * @param pathToSrcRoot Path to the source root
     * @param level JavaParser Language Level
     * @param startingNode Method to start the recursion check from, which may be {@code null}
     * @return Optional.empty() if recursive call is detected, otherwise an error message
     */
    public static Optional<String> hasCycle(Path pathToSrcRoot, ParserConfiguration.LanguageLevel level, Method startingNode) {
        Graph<String, DefaultEdge> graph = createMethodCallGraph(pathToSrcRoot, level);
        return isNotEmpty(checkCycle(graph, startingNode)) ? Optional.empty() : Optional.of("No recursive call detected");
    }

    /**
     * Check if the startingNode has no recursive call
     * @param pathToSrcRoot Path to the source root
     * @param level JavaParser Language Level
     * @param startingNode Method to start the recursion check from, which may be {@code null}
     * @return Optional.empty() if no recursive call is detected, otherwise an error message with methods in the detected cycle
     */
    public static Optional<String> hasNoCycle(Path pathToSrcRoot, ParserConfiguration.LanguageLevel level, Method startingNode) {
        Graph<String, DefaultEdge> graph = createMethodCallGraph(pathToSrcRoot, level);
        return checkCycle(graph, startingNode).stream().reduce((s1, s2) -> String.join(", ", s1, s2));
    }

    /**
     * Check if the graph has a cycle
     * @param graph Method call graph
     * @param startingNode Method to start the recursion check from, which may be {@code null}
     * @return Set of methods in the detected cycle
     */
    private static Set<String> checkCycle(Graph<String, DefaultEdge> graph, Method startingNode) {
        // Convert Method to Node name
        String nodeName = startingNode != null ? startingNode.getDeclaringClass().getName() + "." + startingNode.getName() + getParameters(startingNode) : null;

        if (nodeName != null) {
            Graph<String, DefaultEdge> subgraph = MethodCallGraph.extractSubgraph(graph, nodeName);
            MethodCallGraph.exportToDotFile("subgraph.dot", subgraph);
            return new CycleDetector<>(subgraph).findCycles();
        } else {
            return new CycleDetector<>(graph).findCycles();
        }
    }

    /**
     * Get the parameters of the method
     * @param method Method
     * @return String representation of the parameters
     */
    private static String getParameters(Method method) {
        return "(" + Arrays.stream(method.getParameters()).map(Parameter::getType).map(Class::getName).collect(Collectors.joining(", ")) + ")";
    }

    /**
     * Create a method call graph from the source root
     * @param pathToSrcRoot Path to the source root
     * @param level JavaParser Language Level
     * @return Method call graph
     */
    public static Graph<String, DefaultEdge> createMethodCallGraph(Path pathToSrcRoot, ParserConfiguration.LanguageLevel level) {
        MethodCallGraph methodCallGraph = new MethodCallGraph();
        List<Optional<CompilationUnit>> asts = parseFromSourceRoot(pathToSrcRoot, level);
        for (Optional<CompilationUnit> ast : asts) {
            ast.ifPresent(methodCallGraph::createGraph);
        }

        return methodCallGraph.getGraph();
    }

    /**
     * Parse all Java files in the source root
     * @param pathToSourceRoot Path to the source root
     * @param level JavaParser Language Level
     * @return List of CompilationUnit
     */
    public static List<Optional<CompilationUnit>> parseFromSourceRoot(Path pathToSourceRoot, ParserConfiguration.LanguageLevel level) {
        CombinedTypeSolver combinedTypeSolver = new CombinedTypeSolver();
        combinedTypeSolver.add(new ReflectionTypeSolver());
        combinedTypeSolver.add(new JavaParserTypeSolver(new File(pathToSourceRoot.toString())));
        combinedTypeSolver.add(new ClassLoaderTypeSolver(MethodCallGraph.class.getClassLoader()));

        // Configure JavaParser to use type resolution
        JavaSymbolSolver symbolSolver = new JavaSymbolSolver(combinedTypeSolver);

        SourceRoot sourceRoot = new SourceRoot(pathToSourceRoot, new ParserConfiguration().setSymbolResolver(symbolSolver).setLanguageLevel(level));

        List<ParseResult<CompilationUnit>> parseResults;
        try {
            parseResults = sourceRoot.tryToParse();
        } catch (IOException e) {
            LOG.error("Error reading Java file", e); //$NON-NLS-1$
            throw new AssertionError(String.format("The file %s could not be read:", e));
        }

        return parseResults.stream().map(ParseResult::getResult).toList();
    }
}
