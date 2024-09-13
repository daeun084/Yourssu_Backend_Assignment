package yourssu.backend.domain.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import yourssu.backend.common.exception.GeneralException;
import yourssu.backend.common.security.UserPrincipal;
import yourssu.backend.common.status.ErrorStatus;
import yourssu.backend.domain.converter.CommentConverter;
import yourssu.backend.domain.dto.request.CommentRequest;
import yourssu.backend.domain.dto.response.CommentResponse;
import yourssu.backend.domain.entity.Article;
import yourssu.backend.domain.entity.Comment;
import yourssu.backend.domain.entity.User;
import yourssu.backend.domain.repository.ArticleRepository;
import yourssu.backend.domain.repository.CommentRepository;

@Service
@RequiredArgsConstructor
public class CommentService {
    private final CommentRepository commentRepository;
    private final ArticleRepository articleRepository;

    /*
     * user 정보, articleId, content를 받아 댓글을 작성 후 comment 정보 반환
     * @param request
     * @return
     */
    @Transactional
    public CommentResponse.CommentDto postComment(CommentRequest.PostCommentDto request, UserPrincipal userprincipal) {
        User user = userprincipal.getUser();
        Article article = findArticleById(request.getArticleId());
        String content = validateContent(request.getContent());

        Comment comment = CommentConverter.toComment(content, user, article);
        commentRepository.save(comment);

        return CommentConverter.toCommentDto(comment);
    }

    /*
     * user 정보, content를 받아 본인의 댓글을 수정 후 comment 정보 반환
     * @param request
     * @param commentId
     * @return
     */
    @Transactional
    public CommentResponse.CommentDto patchComment(CommentRequest.PatchCommentDto request, Long commentId, UserPrincipal userprincipal) {
        User user = userprincipal.getUser();
        Comment comment = findCommentById(commentId);
        validateIsUserAuthorized(user, comment);

        String content = validateContent(request.getContent());
        comment.updateContent(content);

        return CommentConverter.toCommentDto(comment);
    }

    /*
     * user 정보, commentId를 받아 본인의 댓글을 삭제
     * @param request
     * @param commentId
     */
    @Transactional
    public void deleteComment(Long commentId, UserPrincipal userprincipal){
        User user = userprincipal.getUser();
        Comment comment = findCommentById(commentId);
        validateIsUserAuthorized(user, comment);

        commentRepository.delete(comment);
    }

    private String validateContent(String content) {
        if (content.isBlank()) {
            throw new GeneralException(ErrorStatus.INVALID_CONTENT);
        }
        return content;
    }

    private void validateIsUserAuthorized(User user, Comment comment) {
        if (!comment.getUser().getUserId().equals(user.getUserId())) {
            throw new GeneralException(ErrorStatus.UNAUTHORIZED_PATCH_COMMENT);
        }
    }

    private Article findArticleById(Long articleId){
        return articleRepository.findById(articleId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.NOT_FOUND_ARTICLE));
    }

    private Comment findCommentById(Long commentId){
        return commentRepository.findById(commentId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.NOT_FOUND_COMMENT));
    }
}
