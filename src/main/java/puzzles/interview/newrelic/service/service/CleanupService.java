package puzzles.interview.newrelic.service.service;

import lombok.AllArgsConstructor;

import javax.inject.Singleton;
import java.util.List;

@Singleton
@AllArgsConstructor
public class CleanupService {

    /**
     * Closes each client's connection for a graceful shutdown of the application.
     *
     * @param numberClients all the clients currently connected to the application.
     */
    public void closeClientConnections(List<NumberClient> numberClients) {

        numberClients.forEach(numberClient ->
                numberClient.terminate("Graceful shutdown of", false)
        );
    }
}
