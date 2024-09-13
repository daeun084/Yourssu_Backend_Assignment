package yourssu.backend.domain.converter;

import yourssu.backend.domain.dto.response.CommentResponse;
import yourssu.backend.domain.entity.Article;
import yourssu.backend.domain.entity.Comment;
import yourssu.backend.domain.entity.User;

public class CommentConverter {

    public static Comment toComment(String content, User user, Article article) {
        return Comment.builder()
                .content(content)
                .user(user)
                .article(article)
                .build();
    }

    public static CommentResponse.CommentDto toCommentDto(Comment comment) {
        return CommentResponse.CommentDto.builder()
                .commentId(comment.getCommentId())
                .email(comment.getUser().getEmail())
                .content(comment.getContent())
                .build();
    }
}
