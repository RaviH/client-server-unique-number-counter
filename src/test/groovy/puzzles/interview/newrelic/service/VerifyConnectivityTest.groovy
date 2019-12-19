package puzzles.interview.newrelic.service

import io.micronaut.context.ApplicationContext
import puzzles.interview.newrelic.TestClient

class VerifyConnectivityTest extends AbstractBaseTest {

    static ApplicationContext applicationContext = null

    def setup() {
        startServer()
    }

    def "verify only 5 client connections are accepted"() {
        given:
        conditions.within(10, {
            verifyLineInOutput("Listening for connections on port: 4002", 1)
        })
        def client1 = new TestClient()
        def client2 = new TestClient()
        def client3 = new TestClient()
        def client4 = new TestClient()
        def client5 = new TestClient()
        def client6 = new TestClient()

        when:
        client1.startConnection("127.0.0.1", 4002)
        client2.startConnection("127.0.0.1", 4002)
        client3.startConnection("127.0.0.1", 4002)
        client4.startConnection("127.0.0.1", 4002)
        client5.startConnection("127.0.0.1", 4002)

        client1.sendMessage("123456789")
        client2.sendMessage("223456789")
        client3.sendMessage("323456789")
        client4.sendMessage("423456789")
        client5.sendMessage("523456789")

        then:
        conditions.within(2, {
            verifyLineInOutput("Reached max capacity of 5 client connections!", 1)
        })

        when:
        client6.startConnection("127.0.0.1", 4002)

        then:
        thrown(ConnectException)

        when:
        client6.sendMessage("445566778")

        then:
        conditions.within(1, {
            verifyLineInOutput("Got a new message: 445566778", 0)
        })

        when: 'other clients can still send message'
        client1.sendMessage("133456789")
        client2.sendMessage("233456789")
        client3.sendMessage("333456789")
        client4.sendMessage("433456789")
        client5.sendMessage("533456789")

        then:
        conditions.within(2, {
            verifyLineInOutput("Got a new message: 133456789", 1)
        })
        conditions.within(2, {
            verifyLineInOutput("Got a new message: 233456789", 1)
        })
        conditions.within(2, {
            verifyLineInOutput("Got a new message: 333456789", 1)
        })
        conditions.within(2, {
            verifyLineInOutput("Got a new message: 433456789", 1)
        })
        conditions.within(2, {
            verifyLineInOutput("Got a new message: 533456789", 1)
        })

        when:
        client1.sendMessage("terminate")
        applicationContext.stop()

        then:
        conditions.within(2, {
            verifyLineInOutput("Graceful shutdown of: Socket connection 1", 1)
            verifyLineInOutput("Graceful shutdown of: Socket connection 2", 1)
            verifyLineInOutput("Graceful shutdown of: Socket connection 3", 1)
            verifyLineInOutput("Graceful shutdown of: Socket connection 4", 1)
            verifyLineInOutput("Graceful shutdown of: Socket connection 5", 1)
        })
        conditions.within(2, {
            verifyLineInOutput("Terminating server", 1)
        })
    }

    static void startServer() {
        Thread.start {
            applicationContext = ApplicationContext.run(["port": 4002], "test")
        }
    }
}
