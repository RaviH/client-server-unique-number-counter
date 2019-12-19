# client-server-unique-number-counter
 Counts unique numbers sent to the server by the clients

Requirements: 
-----------

- [x] The Application must accept input from at most 5 concurrent clients on TCP/IP port 4000.
- [x] Input lines presented to the Application via its socket must either be composed of exactly nine decimal digits (e.g.: 314159265 or 007007009) immediately followed by a server-native newline sequence; or a termination sequence as detailed in #9, below.
- [x] Numbers presented to the Application must include leading zeros as necessary to ensure they are each 9 decimal digits.
- [x] The log file, to be named "numbers.log”, must be created anew and/or cleared when the Application starts.
- [x] Only numbers may be written to the log file. Each number must be followed by a server-native newline sequence.
- [x] No duplicate numbers may be written to the log file.
- [x] Any data that does not conform to a valid line of input should be discarded and the client connection terminated immediately and without comment.
- [x] Every 10 seconds, the Application must print a report to standard output:
- [x] The difference since the last report of the count of new unique numbers that have been received.
- [x] The difference since the last report of the count of new duplicate numbers that have been received.
- [x] The total number of unique numbers received for this run of the Application.
- [x] Example text for #8: Received 50 unique numbers, 2 duplicates. Unique total: 567231
- [x] If any connected client writes a single line with only the word "terminate" followed by a server-native newline sequence, the Application must disconnect all clients and perform a clean shutdown as quickly as possible.
- [x] Clearly state all of the assumptions you made in completing the Application.


Assumptions:
-----------

1. Using /tmp directory as the main directory.


Architecture:
----------

1. `NumberServer` is main class that accepts client connections.
2. `NumberClient` receives messages from client and send async message to `Aggregator`
3. `Aggregator` is responsible for processing the message (check if it's unique) and print the stats every 10s. 
   Also, it's responsible for sending unique numbers to `FileWriter` to write to file.
4. `FileWriter` receives async messages from `Aggregator` and writes to file.
5. `CleanupService` is responsible for closing all client connections gracefully.
6. `RunStats` is the class that holds the stats data for each run / iteration.

How to run:
---------------

To run the application from command line:

1. create the package: `mvn clean package`
2. run the application: `java -jar target/client-server-unique-number-counter-1.0-SNAPSHOT.jar`

To run provided performance test:

1. Run `PerformanceVerifier` class. 

It needs some tidying up but based on my runs, the application is processing 1M messages per 20 seconds.

To run the tests from maven:

1. Run `mvn clean verify`

To run tests from IntelliJ:

1. Run the tests in groovy folder.