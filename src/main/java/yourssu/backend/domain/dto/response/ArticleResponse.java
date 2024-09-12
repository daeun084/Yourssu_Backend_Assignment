package yourssu.backend.domain.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

public class ArticleResponse {

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ArticleDto {
        private Long articleId;
        private String email;
        private String title;
        private String content;
    }
}
