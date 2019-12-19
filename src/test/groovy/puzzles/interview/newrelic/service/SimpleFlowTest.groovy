package puzzles.interview.newrelic.service

import io.micronaut.context.ApplicationContext
import puzzles.interview.newrelic.TestClient

class SimpleFlowTest extends AbstractBaseTest {

    static ApplicationContext applicationContext = null

    def setup() {
        startServer()
    }

    def "happy path"() {
        given:
        conditions.within(10, {
            verifyLineInOutput("Listening for connections on port: 4001", 1)
        })
        def client = new TestClient()
        client.startConnection("127.0.0.1", 4001)

        when:
        client.sendMessage("123456789")

        then:
        conditions.within(4, {
            verifyLineInOutput("Received 1 unique numbers, 0 duplicates. Unique total: 1", 1)
        })

        when:
        client.sendMessage("123456789")

        then:
        conditions.within(4, {
            verifyLineInOutput("Received 0 unique numbers, 1 duplicates. Unique total: 1", 1)
        })

        when:
        client.sendMessage("456123789")
        client.sendMessage("456123789")

        then:
        conditions.within(4, {
            verifyLineInOutput("Received 1 unique numbers, 1 duplicates. Unique total: 2", 1)
        })

        when:
        client.sendMessage("terminate")

        then:
        conditions.within(2, {
            verifyLineInOutput("Terminating client name: Socket connection 1", 1)
        })
        conditions.within(2, {
            verifyLineInOutput("Graceful shutdown of: Socket connection 1", 1)
        })
        conditions.within(2, {
            verifyLineInOutput("Terminating server", 1)
        })

        when:
        def file = new File("/tmp/numbers.log")

        then:
        file.exists()
        conditions.within(2, {
            file.readLines().size() == 2
            file.readLines().contains("123456789")
            file.readLines().contains("456123789")
        })
    }

    static void startServer() {
        Thread.start {
            applicationContext = ApplicationContext.run(["port": 4001], "test")
        }
    }
}
