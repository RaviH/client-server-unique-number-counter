package puzzles.interview.newrelic.service.service

import io.micronaut.context.annotation.Property
import io.micronaut.context.event.ApplicationEventPublisher
import io.micronaut.test.annotation.MicronautTest
import puzzles.interview.newrelic.service.AbstractBaseTest
import puzzles.interview.newrelic.service.event.WriteToFileEvent
import spock.lang.Stepwise

import javax.inject.Inject

@MicronautTest
@Property(name = "start.server", value = "false")
@Property(name = "start.aggregator", value = "false")
@Stepwise
class FileWriterTest extends AbstractBaseTest {

    @Inject
    FileWriter fileWriter

    @Inject
    ApplicationEventPublisher applicationEventPublisher

    def "happy path"() {
        expect:
        new File("/tmp/numbers.log").exists()
        new File("/tmp/numbers.log").size() == 0
    }

    def "verify write to file"() {
        given:
        applicationEventPublisher.publishEvent(new WriteToFileEvent("123456789"))
        applicationEventPublisher.publishEvent(new WriteToFileEvent("223456789"))
        applicationEventPublisher.publishEvent(new WriteToFileEvent("323456789"))

        when:
        def file = new File("/tmp/numbers.log")

        then:
        conditions.within(2, {
            file.readLines().contains("123456789")
            file.readLines().contains("223456789")
            file.readLines().contains("323456789")
        })
    }
}
