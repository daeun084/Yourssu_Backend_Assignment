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
     * 403
     */
    FORBIDDEN_PATCH_ARTICLE(HttpStatus.FORBIDDEN, 403, "게시글 수정 권한이 없습니다."),
    FORBIDDEN_PATCH_COMMENT(HttpStatus.FORBIDDEN, 403, "댓글 수정 권한이 없습니다."),
    FORBIDDEN_WITHDRAWAL(HttpStatus.FORBIDDEN, 403, "계정탈퇴 권한이 없습니다."),

    /**
     * 404
     */
    NOT_FOUND_USER(HttpStatus.NOT_FOUND, 404, "존재하지 않는 유저입니다."),
    NOT_FOUND_ARTICLE(HttpStatus.NOT_FOUND, 404, "존재하지 않는 게시글입니다."),
    NOT_FOUND_COMMENT(HttpStatus.NOT_FOUND, 404, "존재하지 않는 댓글입니다.");




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