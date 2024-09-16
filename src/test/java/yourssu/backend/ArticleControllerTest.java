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
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.web.servlet.MockMvc;
import yourssu.backend.common.exception.GeneralException;
import yourssu.backend.common.security.CustomUserDetailService;
import yourssu.backend.common.security.JwtTokenProvider;
import yourssu.backend.common.security.UserPrincipal;
import yourssu.backend.common.status.ErrorStatus;
import yourssu.backend.domain.controller.ArticleController;
import yourssu.backend.domain.converter.UserConverter;
import yourssu.backend.domain.dto.request.ArticleRequest;
import yourssu.backend.domain.dto.response.ArticleResponse;
import yourssu.backend.domain.dto.response.TokenDto;
import yourssu.backend.domain.entity.User;
import yourssu.backend.domain.service.ArticleService;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.*;
import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.delete;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.patch;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;
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


    private String setAuthentication(String email, String username, String password) {
        User user = UserConverter.toUser(email, username, password);
        UserDetails userDetails = new UserPrincipal(user);
        UsernamePasswordAuthenticationToken authenticationToken =
                new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authenticationToken);

        // token mocking
        TokenDto tokenDto = new TokenDto("AccessToken", "RefreshToken");
        given(jwtTokenProvider.createToken(any(Authentication.class)))
                .willReturn(tokenDto);

        return tokenDto.getAccessToken();
    }

    @Test
    @DisplayName("title, content를 RequestBody로 받아 Article 객체를 생성한 후 작성자의 이메일, 생성된 Article의 PK, title, content를 반환한다.")
    public void createArticle() throws Exception {

        // set authentication
        String authenticationToken = setAuthentication("test@mail.com", "user", "1234");

        // request
        ArticleRequest.ArticleDto request = new ArticleRequest.ArticleDto("Test1", "test1");

        // given
        given(articleService.postArticle(any(), any()))
                .willReturn(new ArticleResponse.ArticleDto(1L, "test@mail.com", "Test1", "test1"));

        // when&then
        mockMvc.perform(post("/api/v1/article")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer "+authenticationToken)
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
                        requestHeaders(
                                headerWithName(HttpHeaders.AUTHORIZATION).description("Bearer token")
                        ),
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
        String authenticationToken = setAuthentication("test@mail.com", "user", "1234");

        // request
        ArticleRequest.ArticleDto request = new ArticleRequest.ArticleDto("  ", "test2");

        // given
        given(articleService.postArticle(any(), any()))
                .willThrow(new GeneralException(ErrorStatus.INVALID_TITLE));

        // when&then
        mockMvc.perform(post("/api/v1/article")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer "+authenticationToken)
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
        String authenticationToken = setAuthentication("test@mail.com", "user", "1234");

        // request
        ArticleRequest.ArticleDto request = new ArticleRequest.ArticleDto("Test3", " ");

        // given
        given(articleService.postArticle(any(), any()))
                .willThrow(new GeneralException(ErrorStatus.INVALID_CONTENT));

        // when&then
        mockMvc.perform(post("/api/v1/article")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer "+authenticationToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().is4xxClientError())
                .andExpect(jsonPath("$.code").value("400"))
                .andExpect(jsonPath("$.result").value("FAILURE"))
                .andExpect(jsonPath("$.message").value("유효하지 않은 content 형식입니다."));
    }

    @Test
    @DisplayName("title, content를 RequestBody로 받아 Article 객체를 수정한 후 작성자의 이메일, 수정된 Article의 PK, title, content를 반환한다.")
    public void patchArticle() throws Exception {
        // set authentication
        String authenticationToken = setAuthentication("test@mail.com", "user", "1234");

        // request
        ArticleRequest.ArticleDto request = new ArticleRequest.ArticleDto("Test4", "test4");

        // given
        given(articleService.patchArticle(any(), eq(1L), any()))
                .willReturn(new ArticleResponse.ArticleDto(1L, "test@mail.com", "Test4", "test4"));

        // when&then
        mockMvc.perform(patch("/api/v1/article/{articleId}", 1L)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer "+authenticationToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("200"))
                .andExpect(jsonPath("$.result").value("SUCCESS"))
                .andExpect(jsonPath("$.message").value("게시물 수정에 성공했습니다."))
                .andExpect(jsonPath("$.data.articleId").value(1L))
                .andExpect(jsonPath("$.data.email").value("test@mail.com"))
                .andExpect(jsonPath("$.data.title").value("Test4"))
                .andExpect(jsonPath("$.data.content").value("test4"))
                .andDo(document("patch-article",
                        requestHeaders(
                                headerWithName(HttpHeaders.AUTHORIZATION).description("Bearer token")
                        ),
                        pathParameters(
                                parameterWithName("articleId").description("Article Id")
                        ),
                        requestFields(
                                fieldWithPath("title").description("수정할 Article의 제목"),
                                fieldWithPath("content").description("수정할 Article의 본문")
                        ),
                        responseFields(
                                fieldWithPath("code").description("상태 코드"),
                                fieldWithPath("result").description("결과 성공 여부"),
                                fieldWithPath("message").description("결과 메시지"),
                                fieldWithPath("data.articleId").description("수정된 Article 객체의 PK"),
                                fieldWithPath("data.email").description("해당 Article을 작성한 사용자의 이메일 주소"),
                                fieldWithPath("data.title").description("수정된 Article의 제목"),
                                fieldWithPath("data.content").description("수정된 Article의 본문")
                        )
                ));
    }

    @Test
    public void patchArticleWithInvalidContent() throws Exception {
        // set authentication
        String authenticationToken = setAuthentication("test@mail.com", "user", "1234");

        // request
        ArticleRequest.ArticleDto request = new ArticleRequest.ArticleDto("Test5", " ");

        // given
        // request로 받는 content가 공백일 경우, 400 error
        given(articleService.patchArticle(any(), eq(1L), any()))
                .willThrow(new GeneralException(ErrorStatus.INVALID_CONTENT));

        // when&then
        mockMvc.perform(patch("/api/v1/article/{articleId}", 1L)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer "+authenticationToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().is4xxClientError())
                .andExpect(jsonPath("$.code").value("400"))
                .andExpect(jsonPath("$.result").value("FAILURE"))
                .andExpect(jsonPath("$.message").value("유효하지 않은 content 형식입니다."));
    }

    @Test
    public void patchArticleWithNonExistArticleId() throws Exception {
        // set authentication
        String authenticationToken = setAuthentication("test@mail.com", "user", "1234");

        // request
        ArticleRequest.ArticleDto request = new ArticleRequest.ArticleDto("Test6", "test6");

        // given
        // 존재하지 않는 article에 대한 id를 pathvariable로 제공할 경우, 404 error
        given(articleService.patchArticle(any(), eq(30L), any()))
                .willThrow(new GeneralException(ErrorStatus.NOT_FOUND_ARTICLE));

        // when&then
        mockMvc.perform(patch("/api/v1/article/{articleId}", 30L)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer "+authenticationToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().is4xxClientError())
                .andExpect(jsonPath("$.code").value("404"))
                .andExpect(jsonPath("$.result").value("FAILURE"))
                .andExpect(jsonPath("$.message").value("존재하지 않는 게시글입니다."));
    }

    @Test
    public void patchArticleWithUnAuthorizedUser() throws Exception {
        // set authentication
        String authenticationToken = setAuthentication("test2@mail.com", "user2", "1234");

        // request
        ArticleRequest.ArticleDto request = new ArticleRequest.ArticleDto("Test7", "test7");

        // given
        // Article을 작성한 유저 정보와 로그인한 유저 정보가 일치하지 않을 경우, 403 error
        given(articleService.patchArticle(any(), eq(1L), any()))
                .willThrow(new GeneralException(ErrorStatus.FORBIDDEN_PATCH_ARTICLE));

        // when&then
        mockMvc.perform(patch("/api/v1/article/{articleId}", 1L)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer "+authenticationToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().is4xxClientError())
                .andExpect(jsonPath("$.code").value("403"))
                .andExpect(jsonPath("$.result").value("FAILURE"))
                .andExpect(jsonPath("$.message").value("게시글 수정 권한이 없습니다."));
    }


    @Test
    @DisplayName("삭제할 Article의 PK를 PathVariable로 받아 Article 객체를 삭제한다.")
    public void deleteArticle() throws Exception {
        // set authentication
        String authenticationToken = setAuthentication("test@mail.com", "user", "1234");

        // given
        willDoNothing().given(articleService).deleteArticle(any(), any());

        // when&then
        mockMvc.perform(delete("/api/v1/article/{articleId}", 1L)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer "+authenticationToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("200"))
                .andExpect(jsonPath("$.result").value("SUCCESS"))
                .andExpect(jsonPath("$.message").value("게시물 삭제에 성공했습니다."))
                .andDo(document("delete-article",
                        requestHeaders(
                                headerWithName(HttpHeaders.AUTHORIZATION).description("Bearer token")
                        ),
                        pathParameters(
                                parameterWithName("articleId").description("삭제할 Article 객체의 PK")
                        ),
                        responseFields(
                                fieldWithPath("code").description("상태 코드"),
                                fieldWithPath("result").description("결과 성공 여부"),
                                fieldWithPath("message").description("결과 메시지")
                        )
                ));
    }

    @Test
    public void deleteArticleWithNonExistArticleId() throws Exception {
        // set authentication
        String authenticationToken = setAuthentication("test@mail.com", "user", "1234");

        // given
        // 존재하지 않는 article에 대한 id를 pathvariable로 제공할 경우, 404 error
        doThrow(new GeneralException(ErrorStatus.NOT_FOUND_ARTICLE))
                .when(articleService).deleteArticle(eq(30L), any());

        // when&then
        mockMvc.perform(delete("/api/v1/article/{articleId}", 30L)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer "+authenticationToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is4xxClientError())
                .andExpect(jsonPath("$.code").value("404"))
                .andExpect(jsonPath("$.result").value("FAILURE"))
                .andExpect(jsonPath("$.message").value("존재하지 않는 게시글입니다."));
    }

    @Test
    public void deleteArticleWithUnAuthorizedUser() throws Exception {
        // set authentication
        String authenticationToken = setAuthentication("test@mail.com", "user", "1234");

        // given
        // Article을 작성한 유저 정보와 로그인한 유저 정보가 일치하지 않을 경우, 403 error
        doThrow(new GeneralException(ErrorStatus.FORBIDDEN_PATCH_ARTICLE))
                .when(articleService).deleteArticle(eq(1L), any());

        // when&then
        mockMvc.perform(delete("/api/v1/article/{articleId}", 1L)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer "+authenticationToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is4xxClientError())
                .andExpect(jsonPath("$.code").value("403"))
                .andExpect(jsonPath("$.result").value("FAILURE"))
                .andExpect(jsonPath("$.message").value("게시글 수정 권한이 없습니다."));
    }

    }
