package puzzles.interview.newrelic.service

import groovy.util.logging.Slf4j
import io.micronaut.context.event.ApplicationEventPublisher
import puzzles.interview.newrelic.service.event.NumberEvent
import puzzles.interview.newrelic.service.service.Aggregator

import java.util.stream.IntStream

@Slf4j
class AggregatorTest extends AbstractBaseTest {

    def applicationEventPublisher = Mock(ApplicationEventPublisher)
    Aggregator aggregator = new Aggregator(applicationEventPublisher)

    def setup() {
        aggregator.reset()
    }

    def "verify message for single pass"() {
        given:
        aggregator != null
        def thread1 = createThread(100000000, 100000200)
        def thread2 = createThread(200000000, 200000200)
        def thread3 = createThread(300000000, 300000200)
        def thread4 = createThread(400000000, 400000200)
        def thread5 = createThread(500000000, 500000200)

        when:
        conditions.within(20, {
            assert !thread1.isAlive()
            assert !thread2.isAlive()
            assert !thread3.isAlive()
            assert !thread4.isAlive()
            assert !thread5.isAlive()
        })
        def message = aggregator.printStats()

        then:
        message
        conditions.within(20, {
            assert message == "Received 1000 unique numbers, 0 duplicates. Unique total: 1000"
        })
    }

    def "verify duplicate count (single pass)"() {
        given:
        def thread1 = createThreadWithDuplicateElements(100000000, 100, 90)
        def thread2 = createThreadWithDuplicateElements(200000000, 100, 80)
        def thread3 = createThreadWithDuplicateElements(300000000, 100, 70)
        def thread4 = createThreadWithDuplicateElements(400000000, 100, 60)
        def thread5 = createThreadWithDuplicateElements(500000000, 100, 50)

        when:
        conditions.within(2, {
            assert !thread1.isAlive()
            assert !thread2.isAlive()
            assert !thread3.isAlive()
            assert !thread4.isAlive()
            assert !thread5.isAlive()
        })
        def message = aggregator.printStats()

        then:
        message
        message == "Received 350 unique numbers, 150 duplicates. Unique total: 350"
    }

    def "verify duplicate count (two passes)"() {
        given:
        def thread1 = createThreadWithDuplicateElements(100000000, 100, 10)
        def thread2 = createThreadWithDuplicateElements(200000000, 100, 20)
        def thread3 = createThreadWithDuplicateElements(300000000, 100, 30)
        def thread4 = createThreadWithDuplicateElements(400000000, 100, 40)
        def thread5 = createThreadWithDuplicateElements(500000000, 100, 50)

        when:
        conditions.within(20, {
            assert !thread1.isAlive()
            assert !thread2.isAlive()
            assert !thread3.isAlive()
            assert !thread4.isAlive()
            assert !thread5.isAlive()
        })
        def message = aggregator.printStats()

        then:
        message
        conditions.within(20, {
            message == "Received 350 unique numbers, 150 duplicates. Unique total: 350"
        })

        when:
        thread1 = createThreadWithDuplicateElements(110000000, 100, 10)
        thread2 = createThreadWithDuplicateElements(210000000, 100, 20)
        thread3 = createThreadWithDuplicateElements(310000000, 100, 30)
        thread4 = createThreadWithDuplicateElements(410000000, 100, 40)
        thread5 = createThreadWithDuplicateElements(510000000, 100, 50)
        conditions.within(20, {
            assert !thread1.isAlive()
            assert !thread2.isAlive()
            assert !thread3.isAlive()
            assert !thread4.isAlive()
            assert !thread5.isAlive()
        })
        message = aggregator.printStats()

        then:
        message
        message == "Received 350 unique numbers, 150 duplicates. Unique total: 700"
    }

    def createThread(int startRange, int endRange) {
        return Thread.start {
            IntStream.range(startRange, endRange).each {
                aggregator.newMessage(new NumberEvent(it))
            }
        }
    }

    def createThreadWithDuplicateElements(int startRange, int numberOfElements, int numberOfUnique) {
        return Thread.start {
            int numberOfDuplicates = numberOfElements - numberOfUnique
            (1..numberOfUnique).forEach {
                def number = startRange + it
                aggregator.newMessage(new NumberEvent(number))
            }

            (1..numberOfDuplicates).forEach {
                def number = startRange + it
                aggregator.newMessage(new NumberEvent(number))
            }
        }
    }
}
