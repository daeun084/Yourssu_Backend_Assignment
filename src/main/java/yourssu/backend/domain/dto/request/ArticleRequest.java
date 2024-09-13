package yourssu.backend.domain.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

public class ArticleRequest {
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ArticleDto {
        @NotBlank
        private String title;
        @NotBlank
        private String content;
    }
}
