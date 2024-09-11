package yourssu.backend.common.status;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;
import yourssu.backend.common.base.BaseErrorCode;
import yourssu.backend.common.base.ErrorReasonDto;

@Getter
@AllArgsConstructor
public enum ErrorStatus implements BaseErrorCode {

    TMP_ERROR(HttpStatus.BAD_REQUEST, 400, "TMP ERROR");

    private final HttpStatus httpStatus;
    private final int code;
    private final String message;

    @Override
    public ErrorReasonDto getReasonHttpStatus() {
        return ErrorReasonDto.builder()
                .message(message)
                .code(code)
                .success(false)
                .build();
    }

    @Override
    public ErrorReasonDto getReason() {
        return ErrorReasonDto.builder()
                .httpStatus(httpStatus)
                .message(message)
                .code(code)
                .success(false)
                .build();
    }
}
