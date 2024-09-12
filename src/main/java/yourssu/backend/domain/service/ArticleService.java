package yourssu.backend.domain.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import yourssu.backend.common.exception.GeneralException;
import yourssu.backend.common.status.ErrorStatus;
import yourssu.backend.domain.converter.ArticleConverter;
import yourssu.backend.domain.dto.request.ArticleRequest;
import yourssu.backend.domain.dto.response.ArticleResponse;
import yourssu.backend.domain.entity.Article;
import yourssu.backend.domain.entity.User;
import yourssu.backend.domain.repository.ArticleRepository;
import yourssu.backend.domain.repository.UserRepository;

@Service
@RequiredArgsConstructor
public class ArticleService {
    private final ArticleRepository articleRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    /*
     * user 정보, title, content를 받아 게시글을 작성 후 article 정보 반환
     * @param articleDto
     * @return
     */
    @Transactional
    public ArticleResponse.ArticleDto postArticle(ArticleRequest.ArticleDto request){
        // 사용자 객체 조회
        User user = validateUserCredentials(request.getEmail(), request.getPassword());

        // dto에서 게시글 정보를 가져와 validation 진행
        String title = validateContent(request.getTitle(), "title");
        String content = validateContent(request.getContent(), "content");

        // 게시글 객체 생성 및 연관관계 설정
        Article article = ArticleConverter.toArticle(title, content, user);
        articleRepository.save(article);
        user.addArticleList(article);

        return ArticleConverter.toArticleDto(article);
    }

    /*
     * user 정보, title, content, articleId를 받아 본인의 게시글을 수정 후 article 정보 반환
     * @param request
     * @param articleId
     * @return
     */
    @Transactional
    public ArticleResponse.ArticleDto patchArticle(ArticleRequest.ArticleDto request, Long articleId){
        // 사용자 객체 조회
        User user = validateUserCredentials(request.getEmail(), request.getPassword());
        // 게시글 조회 후 사용자 본인의 글인지 확인
        Article article = findArticleById(articleId);
        validateIsUserAuthorized(user, article);

        // dto에서 게시글 정보를 가져와 validation 진행
        String title = validateContent(request.getTitle(), "title");
        String content = validateContent(request.getContent(), "content");

        // 게시글 수정
        article.updateTitle(title);
        article.updateContent(content);

        return ArticleConverter.toArticleDto(article);
    }

    private User validateUserCredentials(String email, String password) {
        User user = findUserByEmail(email);

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new GeneralException(ErrorStatus.NOT_MATCH_PASSWORD);
        }
        return user;
    }

    private String validateContent(String content, String fieldName) {
        if (content.isBlank()) {
            ErrorStatus errorStatus = fieldName.equals("title") ? ErrorStatus.INVALID_TITLE : ErrorStatus.INVALID_CONTENT;
            throw new GeneralException(errorStatus);
        }
        return content;
    }

    private void validateIsUserAuthorized(User user, Article article) {
        if (!article.getUser().getUserId().equals(user.getUserId())) {
            throw new GeneralException(ErrorStatus.UNAUTHORIZED_PATCH_ARTICLE);
        }
    }

    private User findUserByEmail(String email){
        return userRepository.findUserByEmail(email)
                .orElseThrow(() -> new GeneralException(ErrorStatus.NOT_FOUND_USER));
    }

    private Article findArticleById(Long articleId){
        return articleRepository.findById(articleId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.NOT_FOUND_ARTICLE));
    }
}
