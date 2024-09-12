package yourssu.backend.domain.converter;

import yourssu.backend.domain.dto.response.ArticleResponse;
import yourssu.backend.domain.entity.Article;
import yourssu.backend.domain.entity.User;

public class ArticleConverter {
    public static Article toArticle(String title, String content, User user){
        return Article.builder()
                .title(title)
                .content(content)
                .user(user)
                .build();
    }

    public static ArticleResponse.ArticleDto toArticleDto(Article article) {
        return ArticleResponse.ArticleDto.builder()
                .articleId(article.getArticleId())
                .email(article.getUser().getEmail())
                .title(article.getTitle())
                .content(article.getContent())
                .build();
    }
}
