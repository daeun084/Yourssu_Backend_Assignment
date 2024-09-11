package yourssu.backend.common.base;

public interface BaseErrorCode {
    ErrorReasonDto getReasonHttpStatus();

    ErrorReasonDto getReason();
}
