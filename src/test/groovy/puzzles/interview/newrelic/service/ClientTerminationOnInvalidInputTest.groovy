package puzzles.interview.newrelic.service

import io.micronaut.context.ApplicationContext
import puzzles.interview.newrelic.TestClient

class ClientTerminationOnInvalidInputTest extends AbstractBaseTest {

    static ApplicationContext applicationContext = null

    def setup() {
        startServer()
    }

    def cleanupSpec() {
        if (applicationContext != null) {
            applicationContext.close()
        }
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
        client.sendMessage("9")
        client.sendMessage("223456789")

        then:
        conditions.within(2, {
            verifyLineInOutput("Got a new message: 9", 0)
            verifyLineInOutput("Got a new message: 223456789", 0)
            verifyLineInOutput("Graceful shutdown of client connection: Socket connection 1", 1)
        })
    }

    static void startServer() {
        Thread.start {
            applicationContext = ApplicationContext.run(["port": 4001], "test")
        }
    }
}
