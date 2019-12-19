package puzzles.interview.newrelic.service.service;

import io.micronaut.context.annotation.Requires;
import io.micronaut.context.annotation.Value;
import io.micronaut.context.event.ApplicationEventPublisher;
import io.micronaut.runtime.event.annotation.EventListener;
import io.micronaut.scheduling.annotation.Async;
import io.micronaut.scheduling.annotation.Scheduled;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import puzzles.interview.newrelic.service.data.RunStats;
import puzzles.interview.newrelic.service.event.NumberEvent;
import puzzles.interview.newrelic.service.event.WriteToFileEvent;

import javax.inject.Singleton;
import java.util.concurrent.ConcurrentHashMap;

@Singleton
@Slf4j
@Requires(property = "start.aggregator", value = "true")
public class Aggregator {

    private final ApplicationEventPublisher applicationEventPublisher;
    private ConcurrentHashMap<Integer, Integer> numbers = new ConcurrentHashMap<>();
    private final RunStats runStats = new RunStats();

    @Value("${print.message:false}")
    private boolean printMessage;

    public Aggregator(final ApplicationEventPublisher applicationEventPublisher) {

        this.applicationEventPublisher = applicationEventPublisher;
    }

    /**
     * Receives new message (a number) and adds it to the hash map as key.
     * If the put operation returns null, means it's a new entry, else it's a duplicate.
     * Increment counts accordingly.
     *
     * @param numberEvent
     */
    @EventListener
    @Async
    public void newMessage(NumberEvent numberEvent) {

        final int number = numberEvent.getNumber();
        if (printMessage) {
            // Purely for testing purpose.
            log.debug("Got a new message: {}", number);
        }
        final Integer putResult = numbers.put(number, 1);
        /*
         * If it's a unique number then increment unique count and
         * fire an event to write to output file, else simply increment
         * duplicate count.
         */
        if (isUniqueNumber(putResult)) {
            runStats.incrementCurrentUniqueCount();
            applicationEventPublisher.publishEvent(new WriteToFileEvent(String.valueOf(number)));
        } else {
            runStats.incrementCurrentDuplicateCount();
        }
    }

    private boolean isUniqueNumber(final Integer putResult) {

        return putResult == null;
    }

    /**
     * Prints stats to output.
     *
     * @return string printed to output
     */
    @Scheduled(initialDelay = "${print.stats.initial.delay:10s}", fixedDelay = "${print.stats.delay:10s}")
    public String printStats() {

        val totalUniqueElements = numbers.keySet().size();

        val message = String.format(
                "Received %d unique numbers, %d duplicates. Unique total: %d",
                runStats.getUniqueNumbers(),
                runStats.getDuplicateNumbers(),
                totalUniqueElements
        );
        log.info(message);
        runStats.resetForNextIteration();
        return message;
    }

    /**
     * Used for test purpose only.
     */
    public void reset() {

        runStats.reset();
        numbers = new ConcurrentHashMap<>();
    }
}
