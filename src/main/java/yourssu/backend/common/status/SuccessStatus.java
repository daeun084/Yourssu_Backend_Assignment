package yourssu.backend.common.status;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum SuccessStatus {

    SUCCESS(HttpStatus.OK, 200, "요청이 정상적으로 처리되었습니다."),

    /**
     * User
     */
    SIGN_UP_SUCCESS(HttpStatus.OK, 200, "회원가입에 성공했습니다."),

    /**
     * Article
     */
    ARTICLE_POST_SUCCESS(HttpStatus.OK, 200, "게시물 작성에 성공했습니다.");


    private final HttpStatus httpStatus;
    private final int code;
    private final String message;
}
