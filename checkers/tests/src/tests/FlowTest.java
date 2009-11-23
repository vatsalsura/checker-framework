package tests;

import java.util.Collection;

import org.junit.runners.Parameterized.Parameters;

/**
 */
public class FlowTest extends ParameterizedCheckerTest {

    public FlowTest(String testName) {
        super(testName, "checkers.util.test.FlowTestChecker", "flow");
    }

    @Parameters
    public static Collection<Object[]> data() { return testFiles("flow"); }
}
