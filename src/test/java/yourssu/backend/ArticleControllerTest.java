package yourssu.backend;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.web.servlet.MockMvc;
import yourssu.backend.common.exception.GeneralException;
import yourssu.backend.common.security.CustomUserDetailService;
import yourssu.backend.common.security.JwtTokenProvider;
import yourssu.backend.common.security.UserPrincipal;
import yourssu.backend.common.status.ErrorStatus;
import yourssu.backend.domain.controller.ArticleController;
import yourssu.backend.domain.controller.UserController;
import yourssu.backend.domain.converter.UserConverter;
import yourssu.backend.domain.dto.request.ArticleRequest;
import yourssu.backend.domain.dto.request.UserRequest;
import yourssu.backend.domain.dto.response.ArticleResponse;
import yourssu.backend.domain.dto.response.UserResponse;
import yourssu.backend.domain.entity.User;
import yourssu.backend.domain.service.ArticleService;
import yourssu.backend.domain.service.UserService;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureRestDocs
@WebMvcTest(controllers = ArticleController.class, excludeAutoConfiguration = {SecurityAutoConfiguration.class})
@AutoConfigureMockMvc(addFilters = false)
public class ArticleControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ArticleService articleService;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    @MockBean
    private JpaMetamodelMappingContext jpaMappingContext;

    @MockBean
    private CustomUserDetailService customUserDetailService;


    private UsernamePasswordAuthenticationToken setAuthentication(String email, String username, String password) {
        User user = UserConverter.toUser(email, username, password);
        UserDetails userDetails = new UserPrincipal(user);
        UsernamePasswordAuthenticationToken authenticationToken =
                new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authenticationToken);
        return authenticationToken;
    }

    @Test
    @DisplayName("title, content를 RequestBody로 받아 Article 객체를 생성한 후 작성자의 이메일, 생성된 Article의 PK, title, content를 반환한다.")
    public void createArticle() throws Exception {

        // set authentication
        UsernamePasswordAuthenticationToken authenticationToken = setAuthentication("test@mail.com", "user", "1234");

        // request
        ArticleRequest.ArticleDto request = new ArticleRequest.ArticleDto("Test1", "test1");

        // given
        given(articleService.postArticle(any(), any()))
                .willReturn(new ArticleResponse.ArticleDto(1L, "test@mail.com", "Test1", "test1"));

        // when&then
        mockMvc.perform(post("/api/v1/article")
                        .with(authentication(authenticationToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("200"))
                .andExpect(jsonPath("$.result").value("SUCCESS"))
                .andExpect(jsonPath("$.message").value("게시물 작성에 성공했습니다."))
                .andExpect(jsonPath("$.data.articleId").value(1L))
                .andExpect(jsonPath("$.data.email").value("test@mail.com"))
                .andExpect(jsonPath("$.data.title").value("Test1"))
                .andExpect(jsonPath("$.data.content").value("test1"))
                .andDo(document("post-article",
                        requestFields(
                                fieldWithPath("title").description("작성할 Article의 제목"),
                                fieldWithPath("content").description("작성할 Article의 본문")
                        ),
                        responseFields(
                                fieldWithPath("code").description("상태 코드"),
                                fieldWithPath("result").description("결과 성공 여부"),
                                fieldWithPath("message").description("결과 메시지"),
                                fieldWithPath("data.articleId").description("생성된 Article 객체의 PK"),
                                fieldWithPath("data.email").description("해당 Article을 작성한 사용자의 이메일 주소"),
                                fieldWithPath("data.title").description("생성된 Article의 제목"),
                                fieldWithPath("data.content").description("생성된 Article의 본문")
                        )
                ));
    }

    @Test
    public void createArticleWithInvalidTitle() throws Exception {
        // set authentication
        UsernamePasswordAuthenticationToken authenticationToken = setAuthentication("test@mail.com", "user", "1234");

        // request
        ArticleRequest.ArticleDto request = new ArticleRequest.ArticleDto("  ", "test2");

        // given
        given(articleService.postArticle(any(), any()))
                .willThrow(new GeneralException(ErrorStatus.INVALID_TITLE));

        // when&then
        mockMvc.perform(post("/api/v1/article")
                        .with(authentication(authenticationToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().is4xxClientError())
                .andExpect(jsonPath("$.code").value("400"))
                .andExpect(jsonPath("$.result").value("FAILURE"))
                .andExpect(jsonPath("$.message").value("유효하지 않은 title 형식입니다."));
    }

    @Test
    public void createArticleWithInvalidContent() throws Exception {
        // set authentication
        UsernamePasswordAuthenticationToken authenticationToken = setAuthentication("test@mail.com", "user", "1234");

        // request
        ArticleRequest.ArticleDto request = new ArticleRequest.ArticleDto("Test3", " ");

        // given
        given(articleService.postArticle(any(), any()))
                .willThrow(new GeneralException(ErrorStatus.INVALID_CONTENT));

        // when&then
        mockMvc.perform(post("/api/v1/article")
                        .with(authentication(authenticationToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().is4xxClientError())
                .andExpect(jsonPath("$.code").value("400"))
                .andExpect(jsonPath("$.result").value("FAILURE"))
                .andExpect(jsonPath("$.message").value("유효하지 않은 content 형식입니다."));
    }


}
