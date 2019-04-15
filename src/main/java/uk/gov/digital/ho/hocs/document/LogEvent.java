package uk.gov.digital.ho.hocs.document;

public enum LogEvent {
    DOCUMENT_CONVERSION_FAILURE,
    DOCUMENT_CONVERSION_SUCCESS,
    DOCUMENT_CONVERSION_INVALID_FORMAT,
    UNCAUGHT_EXCEPTION;

    public static final String EVENT = "event_id";
    public static final String EXCEPTION = "exception";
}
