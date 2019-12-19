package puzzles.interview.newrelic.service.event;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class WriteToFileEvent {

    private String number;

    public String getNumber() {

        return this.number + System.lineSeparator();
    }
}
