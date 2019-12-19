package puzzles.interview.newrelic.service

import org.junit.Rule
import org.springframework.boot.test.rule.OutputCapture
import spock.lang.Shared
import spock.lang.Specification
import spock.util.concurrent.PollingConditions

abstract class AbstractBaseTest extends Specification {

    @Rule
    OutputCapture outputCapture = new OutputCapture()

    static PollingConditions conditions = new PollingConditions(timeout: 20, initialDelay: 0.1, factor: 0.1)

    @Shared
    def lineSeparator = System.properties['line.separator']

    void verifyLineInOutput(String expectedLine, int expectedCount) {
        def outputLines2 = outputCapture.toString().tokenize(lineSeparator as Character)
        assert outputLines2.findAll { it.contains(expectedLine) }.size() == expectedCount
    }

    void verifyLineInOutputAtLeast(String expectedLine, int expectedCount) {
        def outputLines2 = outputCapture.toString().tokenize(lineSeparator as Character)
        assert outputLines2.findAll { it.contains(expectedLine) }.size() >= expectedCount
    }
}
