package yourssu.backend.common.status;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;
import yourssu.backend.common.base.BaseErrorCode;
import yourssu.backend.common.base.ErrorReasonDto;

@Getter
@AllArgsConstructor
public enum ErrorStatus implements BaseErrorCode {

    /**
     * 400
     */
    TMP_ERROR(HttpStatus.BAD_REQUEST, 400, "TMP ERROR"),

    INVALID_EMAIL(HttpStatus.BAD_REQUEST, 400, "이메일 형식이 올바르지 않습니다."),
    DUPLICATE_USERNAME(HttpStatus.BAD_REQUEST, 400, "해당 유저 이름이 이미 존재합니다."),
    DUPLICATE_EMAIL(HttpStatus.BAD_REQUEST, 400, "해당 이메일로 생성된 계정이 이미 존재합니다."),
    NOT_MATCH_PASSWORD(HttpStatus.BAD_REQUEST, 400, "비밀번호가 일치하지 않습니다."),

    INVALID_TITLE(HttpStatus.BAD_REQUEST, 400, "유효하지 않은 title 형식입니다."),
    INVALID_CONTENT(HttpStatus.BAD_REQUEST, 400, "유효하지 않은 content 형식입니다."),

    /**
     * 404
     */
    NOT_FOUND_USER(HttpStatus.NOT_FOUND, 404, "존재하지 않는 유저입니다.");




    private final HttpStatus httpStatus;
    private final int code;
    private final String message;

    @Override
    public ErrorReasonDto getReasonHttpStatus() {
        return ErrorReasonDto.builder()
                .httpStatus(httpStatus)
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