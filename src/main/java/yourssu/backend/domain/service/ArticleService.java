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
    public ArticleResponse.ArticleDto postArticle(ArticleRequest.ArticleDto articleDto){
        // 사용자 객체 조회
        String email = articleDto.getEmail();
        String password = articleDto.getPassword();
        User user = validateUserCredentials(email, password);

        // dto에서 게시글 정보를 가져와 validation 진행
        String title = articleDto.getTitle();
        String content = articleDto.getContent();

        validateContent(title, "title");
        validateContent(content, "content");

        // 게시글 객체 생성 및 연관관계 설정
        Article article = ArticleConverter.toArticle(title, content, user);
        articleRepository.save(article);
        user.addArticleList(article);

        return ArticleConverter.toArticleDto(article);
    }

    private User validateUserCredentials(String email, String password) {
        User user = findUserByEmail(email);
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new GeneralException(ErrorStatus.NOT_MATCH_PASSWORD);
        }
        return user;
    }

    private User findUserByEmail(String email){
        return userRepository.findUserByEmail(email)
                .orElseThrow(() -> new GeneralException(ErrorStatus.NOT_FOUND_USER));
    }

    private void validateContent(String content, String role) {
        ErrorStatus errorStatus = role.equals("title") ? ErrorStatus.INVALID_TITLE : ErrorStatus.INVALID_CONTENT;

        if (content.isEmpty() || content.isBlank()) {
            throw new GeneralException(errorStatus);
        }
    }
}
