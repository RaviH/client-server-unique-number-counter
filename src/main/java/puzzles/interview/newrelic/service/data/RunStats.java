package puzzles.interview.newrelic.service.data;

import lombok.Getter;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Wrapper class that holds data around each interval/run.
 */
@Getter
public class RunStats {

    final AtomicInteger currentUniqueCount = new AtomicInteger(0);
    final AtomicInteger currentDuplicateCount = new AtomicInteger(0);
    final AtomicInteger pastDuplicateCount = new AtomicInteger(0);
    final AtomicInteger pastUniqueCount = new AtomicInteger(0);

    /**
     * Increment current run's unique count.
     */
    public void incrementCurrentUniqueCount() {

        currentUniqueCount.incrementAndGet();
    }

    /**
     * Increment current run's duplicate count.
     */
    public void incrementCurrentDuplicateCount() {

        currentDuplicateCount.incrementAndGet();
    }

    /**
     * Get unique number from this run.
     *
     * @return unique number from this run.
     */
    public int getUniqueNumbers() {

        return currentUniqueCount.addAndGet(-pastUniqueCount.get());
    }

    /**
     * Get duplicate numbers from this run.
     *
     * @return duplicate number from this run.
     */
    public int getDuplicateNumbers() {

        return currentDuplicateCount.addAndGet(-pastDuplicateCount.get());
    }

    /**
     * Get ready for next iteration.
     */
    public void resetForNextIteration() {

        pastUniqueCount.set(currentUniqueCount.get());
        pastDuplicateCount.set(currentDuplicateCount.get());
    }

    /**
     * Complete reset. Used for tests only.
     */
    public void reset() {

        currentUniqueCount.set(0);
        currentDuplicateCount.set(0);
        pastDuplicateCount.set(0);
        pastUniqueCount.set(0);
    }
}
