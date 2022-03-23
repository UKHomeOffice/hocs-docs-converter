package uk.gov.digital.ho.hocs.document.application;

public enum LogEvent {
    DOCUMENT_CONVERSION_FAILURE,
    DOCUMENT_CONVERSION_SUCCESS,
    DOCUMENT_CONVERSION_INVALID_FORMAT,
    DOCUMENT_CONVERSION_MSG_PARSE_FAILURE,
    DOCUMENT_CONVERSION_MSG_ATTACHMENT_PARSE_FAILURE,
    DOCUMENT_CONVERSION_UNCAUGHT_EXCEPTION;

    public static final String EVENT = "event_id";
    public static final String EXCEPTION = "exception";
    public static final String STACKTRACE = "stacktrace";
}
