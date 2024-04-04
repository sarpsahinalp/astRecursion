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
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public class RecursionCheck {

    private static final Logger LOG = LoggerFactory.getLogger(RecursionCheck.class);

    // Method to determine if there is a cycle in the Graph starting with the node with the given name
    public static Optional<String> hasCycle(Path pathToSrcRoot, ParserConfiguration.LanguageLevel level) {
        Graph<String, DefaultEdge> graph = createMethodCallGraph(pathToSrcRoot, level);
        return checkCycle(graph).stream().reduce(String::concat);
    }

    public static Optional<String> hasNoCycle(Path pathToSrcRoot, ParserConfiguration.LanguageLevel level) {
        Graph<String, DefaultEdge> graph = createMethodCallGraph(pathToSrcRoot, level);
        return checkCycle(graph).stream().reduce(String::concat);
    }

    private static Set<String> checkCycle(Graph<String, DefaultEdge> graph) {
        CycleDetector<String, DefaultEdge> detector = new CycleDetector<>(graph);
        return detector.findCycles();
    }

    public static Graph<String, DefaultEdge> createMethodCallGraph(Path pathToSrcRoot, ParserConfiguration.LanguageLevel level) {
        MethodCallGraph methodCallGraph = new MethodCallGraph();
        List<Optional<CompilationUnit>> asts = parseFromSourceRoot(pathToSrcRoot, level);
        for (Optional<CompilationUnit> ast : asts) {
            ast.ifPresent(methodCallGraph::createGraph);
        }

        return methodCallGraph.getGraph();
    }

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
