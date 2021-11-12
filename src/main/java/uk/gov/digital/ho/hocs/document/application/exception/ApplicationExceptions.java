package uk.gov.digital.ho.hocs.document.application.exception;

import uk.gov.digital.ho.hocs.document.application.LogEvent;

public interface ApplicationExceptions {

    class DocumentFormatException extends RuntimeException {
        private final LogEvent event;
        private final LogEvent exception;

        public DocumentFormatException(String msg, LogEvent event, Object... args) {
            super(String.format(msg, args));
            this.event = event;
            this.exception = null;
        }

        public DocumentFormatException(String msg, LogEvent event, LogEvent exception, Object... args) {
            super(String.format(msg, args));
            this.event = event;
            this.exception = exception;
        }

        public LogEvent getException() { return  exception; }

        public LogEvent getEvent() {
            return event;
        }
    }

    class DocumentConversionException extends RuntimeException {
        private final LogEvent event;
        private final LogEvent exception;

        public DocumentConversionException(String msg, LogEvent event, Object... args) {
            super(String.format(msg, args));
            this.event = event;
            this.exception = null;
        }

        public DocumentConversionException(String msg, LogEvent event, LogEvent exception, Object... args) {
            super(String.format(msg, args));
            this.event = event;
            this.exception = exception;
        }

        public LogEvent getException() { return  exception; }

        public LogEvent getEvent() {
            return event;
        }
    }
}
