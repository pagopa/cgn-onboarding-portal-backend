package it.gov.pagopa.cgn.portal;


import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class LogMemoryAppender extends ListAppender<ILoggingEvent> {
    public void reset() {
        this.list.clear();
    }

    public boolean contains(String string, Level level) {
        return this.list.stream()
                .anyMatch(event -> (event.getMessage().contains(string)
                        || event.getThrowableProxy().getMessage().contains(string))
                        && event.getLevel().equals(level));
    }

    public List<ILoggingEvent> search(String string, Level level) {
        return this.list.stream()
                .filter(event -> (event.getMessage().contains(string)
                        || event.getThrowableProxy().getMessage().contains(string))
                        && event.getLevel().equals(level))
                .collect(Collectors.toList());
    }

    public List<ILoggingEvent> getLoggedEvents() {
        return Collections.unmodifiableList(this.list);
    }
}
