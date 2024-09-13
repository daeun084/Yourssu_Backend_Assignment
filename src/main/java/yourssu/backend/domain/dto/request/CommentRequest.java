package yourssu.backend.domain.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

public class CommentRequest {
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CommentDto {
        @NotBlank
        private String email;
        @NotBlank
        private String password;
        @NotBlank
        private Long articleId;
        @NotBlank
        private String content;
    }
}
