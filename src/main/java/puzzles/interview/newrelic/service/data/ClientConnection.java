package puzzles.interview.newrelic.service.data;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * Wrapper class that provides functionality around client connection like
 * read from and write to client socket.
 */
@Data
@AllArgsConstructor
@Slf4j
public class ClientConnection {

    private BufferedReader bufferedReader;
    private PrintWriter out;

    /**
     * Reads from client socket.
     *
     * @return Data just read from client socket.
     * @throws IOException
     */
    public String readLine() throws IOException {

        return bufferedReader.readLine();
    }

    /**
     * Reply back to the client.
     *
     * @param reply reply message.
     */
    public void reply(String reply) {

        out.println(reply);
    }

    /**
     * Help cleanup client connection.
     */
    public void cleanup() {

        closePrintWriter();
        closeBufferedReader();
    }

    private void closeBufferedReader() {

        if (bufferedReader != null) {
            try {
                bufferedReader.close();
            } catch (IOException e) {
            }
        }
    }

    private void closePrintWriter() {

        if (out != null) {
            out.close();
        }
    }
}
