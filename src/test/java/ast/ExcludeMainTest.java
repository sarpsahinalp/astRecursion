package ast;

import ast.asserting.UnwantedNodesAssert;
import ast.type.LoopType;
import com.github.javaparser.ParserConfiguration;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.Optional;

@SuppressWarnings("OptionalAssignedToNull")
public class ExcludeMainTest {

    public static Optional<AssertionError> noLoopException = null;

    public static void noLoop() throws AssertionError{
        if(noLoopException == null) {
            try {
                UnwantedNodesAssert.assertThatSourcesIn(Path.of("/home/sarps/IdeaProjects/astRecursion/src/main/java/org/example/ExcludeMain")).withLanguageLevel(ParserConfiguration.LanguageLevel.JAVA_17).hasNo(LoopType.ANY);
                noLoopException = Optional.empty();
            } catch (AssertionError ae){
                noLoopException = Optional.of(ae);
                throw noLoopException.get();
            }
        } else {
            if (noLoopException.isPresent()) {
                throw noLoopException.get();
            }
        }
    }

    @Test
    void testNoLoop() {
        noLoop();
    }
}
