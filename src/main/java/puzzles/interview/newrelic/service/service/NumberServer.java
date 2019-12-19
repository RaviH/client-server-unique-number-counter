package puzzles.interview.newrelic.service.service;

import io.micronaut.context.ApplicationContext;
import io.micronaut.context.annotation.Requires;
import io.micronaut.context.event.ApplicationEventPublisher;
import io.micronaut.context.event.StartupEvent;
import io.micronaut.runtime.event.annotation.EventListener;
import lombok.extern.slf4j.Slf4j;
import puzzles.interview.newrelic.service.event.StopServerEvent;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Actual server class that accepts connections from the clients.
 */
@Singleton
@Slf4j
@Requires(property = "start.server", value = "true")
public class NumberServer {

    private int port;

    @Inject
    private ApplicationEventPublisher applicationEventPublisher;

    @Inject
    private ApplicationContext applicationContext;

    @Inject
    private CleanupService cleanupService;

    private List<NumberClient> numberClients = new ArrayList<>();
    private int maxConnections;
    private volatile boolean continueRunning = true;
    private AtomicInteger currentTotal = new AtomicInteger(0);
    private ServerSocket serverSocket;

    public NumberServer(
            final ApplicationContext applicationContext,
            final ApplicationEventPublisher applicationEventPublisher,
            final CleanupService cleanupService
    ) {

        this.applicationContext = applicationContext;
        this.applicationEventPublisher = applicationEventPublisher;
        this.cleanupService = cleanupService;
        this.port = applicationContext.get("port", Integer.class).orElse(9999);
        this.maxConnections = applicationContext.get("max.connections", Integer.class).orElse(5);

        serverSocket = createServerSocket();
    }

    @EventListener
    public void onStartup(StartupEvent startupEvent) throws IOException {

        while (continueRunning && serverSocket != null && !serverSocket.isClosed()) {
            if (currentTotal.get() < maxConnections) {
                Socket socket = acceptConnection(serverSocket);
                // new thread for a client
                final String name = "Socket connection " + currentTotal.get();
                final NumberClient numberClient = new NumberClient(
                        name,
                        socket,
                        applicationEventPublisher
                );
                numberClients.add(numberClient);
                numberClient.start();
            } else {
                continueRunning = false;
                serverSocket.close();
            }
        }
    }

    /**
     * Creates the server socket that the clients would connect to.
     *
     * @return server socket that the clients would connect to.
     */
    private ServerSocket createServerSocket() {

        ServerSocket serverSocket = null;
        try {
            serverSocket = new ServerSocket(port, 5);
        } catch (IOException e) {
            log.error("Error starting NumberServer at port: {}", port, e);
        }

        log.info("Listening for connections on port: {}", port);
        return serverSocket;
    }

    /**
     * In case a client receives "terminate" message, it will fire {@link StopServerEvent}
     * This method handles closing out all the clients, and then stopping the server itself.
     *
     * @param stopServerEvent stop the server event
     * @throws IOException
     */
    @EventListener
    public void stopServer(StopServerEvent stopServerEvent) throws IOException {

        cleanupService.closeClientConnections(numberClients);
        continueRunning = false;
        try {
            new Socket("localhost", 4000);
        } catch (IOException e) {
        }
        serverSocket.close();
        log.info("Terminating server");
        applicationContext.stop();
        log.info("Application shutdown");
    }

    /**
     * Responsible for accepting connections from the client.
     *
     * @param serverSocket server socket object that will accept client connection.
     * @return Client socket object that clients can send message to.
     */
    private Socket acceptConnection(final ServerSocket serverSocket) {

        Socket socket = null;
        try {
            socket = serverSocket.accept();
            final int currConnCount = currentTotal.incrementAndGet();
            log.info("Accepted connection #{}", currentTotal.get());
            if (currConnCount == 5) {
                log.info("Reached max capacity of 5 client connections!");
            }
        } catch (IOException e) {
            log.error("Error accepting connection:", e);
        }
        return socket;
    }
}
