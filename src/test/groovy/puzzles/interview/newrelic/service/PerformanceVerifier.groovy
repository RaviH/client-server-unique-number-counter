package puzzles.interview.newrelic.service

import groovy.util.logging.Slf4j
import io.micronaut.context.ApplicationContext
import puzzles.interview.newrelic.TestClient

import java.util.stream.IntStream

@Slf4j
class PerformanceVerifier extends AbstractBaseTest {

    static ApplicationContext applicationContext = null

    def setup() {
        startServer()
    }

    def "verify performance"() {
        expect:
        conditions.within(10, {
            verifyLineInOutput("Listening for connections on port: 4001", 1)
        })
        createThread(1, 0, 50_000)
        createThread(2, 50_000, 100_000)
        createThread(3, 100_000, 150_000)
        createThread(4, 150_000, 200_000)
        createThread(5, 200_000, 250_000)
        createThread(6, 250_000, 300_000)
        createThread(7, 300_000, 350_000)
        createThread(8, 350_000, 400_000)
        createThread(9, 400_000, 450_000)
        createThread(10, 450_000, 500_000)
        createThread(11, 500_000, 550_000)
        createThread(12, 550_000, 600_000)
        createThread(13, 600_000, 650_000)
        createThread(14, 650_000, 700_000)
        createThread(15, 700_000, 750_000)
        createThread(16, 750_000, 800_000)
        createThread(17, 800_000, 850_000)
        createThread(18, 850_000, 900_000)
        createThread(19, 900_000, 950_000)
        createThread(20, 950_000, 1_000_000)

        conditions.within(60, {
            verifyLineInOutput("Completed sending messages for client: 10", 1)
            verifyLineInOutput("Completed sending messages for client: 2", 1)
            verifyLineInOutput("Completed sending messages for client: 3", 1)
            verifyLineInOutput("Completed sending messages for client: 4", 1)
            verifyLineInOutputAtLeast("Unique total: 1000000", 1)
        })

    }

    def createThread(int name, int startRange, int endRange) {
        def client = new TestClient()
        client.startConnection("127.0.0.1", 4001)

        return Thread.start {
            IntStream.range(startRange, endRange).each {

                def message = String.format("%09d", it)
                client.sendMessage(message)
            }
            log.info("Completed sending messages for client: $name")
        }
    }

    static void startServer() {
        Thread.start {
            applicationContext = ApplicationContext.run(["port"                     : 4001,
                                                         "max.connections"          : 200,
                                                         "print.message"            : false,
                                                         "print.stats.initial.delay": "0s",
                                                         "print.stats.delay"        : "10s"], "test")
        }
    }
}
