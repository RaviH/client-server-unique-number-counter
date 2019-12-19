package puzzles.interview.newrelic.service;

import io.micronaut.runtime.Micronaut;

/**
 * Simple application class.
 */
public final class Application {

    /**
     * Utility classes should not have a public or default constructor -- Courtesy find bugs
     */
    private Application() { }

    public static void main(String[] args) {

        Micronaut.build("").start();
    }
}