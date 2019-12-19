package puzzles.interview.newrelic.service.service;

import io.micronaut.context.event.ApplicationEventPublisher;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import puzzles.interview.newrelic.service.data.ClientConnection;
import puzzles.interview.newrelic.service.event.NumberEvent;
import puzzles.interview.newrelic.service.event.StopServerEvent;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
public class NumberClient extends Thread {

    private String name;
    protected Socket socket;
    private ApplicationEventPublisher eventPublisher;
    private AtomicBoolean continueRunning = new AtomicBoolean(true);

    public NumberClient(String name, Socket socket, ApplicationEventPublisher eventPublisher) {

        this.name = name;
        this.socket = socket;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public void run() {

        Optional<ClientConnection> clientConnectionOpt = getClientConnection();

        if (!clientConnectionOpt.isPresent()) {
            log.warn("Connection with server failed!");
            return;
        }

        clientConnectionOpt.ifPresent(clientConnection -> {
            while (continueRunning.get() && !socket.isClosed()) {
                try {
                    String line = clientConnection.readLine();
                    processMessage(line);
                    clientConnection.reply(line);
                } catch (IOException e) {
                    log.error("Exception occurred while receiving messages:", e);
                    return;
                }
            }

            clientConnection.cleanup();
            log.info("Graceful shutdown of client connection: {}", name);
        });
    }

    private void processMessage(final String line) {

        if (invalidInput(line)) {
            // if invalid input terminate silently.
            terminate("", false);
        } else if (isTerminate(line)) {

            // Client sent terminate message
            terminate("Terminating client name", true);
        } else {

            // happy path, got a number, publish the number so
            // Aggregator can process it.
            final int number = Integer.parseInt(line);
            eventPublisher.publishEvent(new NumberEvent(number));
        }
    }

    /**
     * Stop the thread that receives message message from a client.
     *
     * @param message    log message if not blank
     * @param stopServer stop the server too?
     */
    public void terminate(final String message, final boolean stopServer) {

        if (StringUtils.isNotBlank(message)) {
            log.info("{}: {}", message, name);
        }
        continueRunning.set(false);
        closeSocket();
        if (stopServer) {
            eventPublisher.publishEvent(new StopServerEvent());
        }
    }

    /**
     * Cleanup socket.
     */
    private void closeSocket() {

        try {
            socket.close();
        } catch (IOException e) {
            log.error("Error: ", e);
        }
    }

    /**
     * Get connection to the client and return wrapper {@link ClientConnection} class.
     *
     * @return client connection wrapper class.
     */
    public Optional<ClientConnection> getClientConnection() {

        try {
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            return Optional.of(new ClientConnection(bufferedReader, out));
        } catch (IOException e) {
            log.error("Error occurred while getting input stream from socket: ", e);
            return Optional.empty();
        }
    }

    private boolean isTerminate(final String line) {

        return line.equals("terminate");
    }

    /**
     * Message should not be null or not 9 in length.
     *
     * @param line message from client
     * @return true if message is invalid.
     */
    private boolean invalidInput(final String line) {

        return line == null || line.length() != 9;
    }
}