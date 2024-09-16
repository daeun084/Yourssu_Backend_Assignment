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
import yourssu.backend.domain.controller.CommentController;
import yourssu.backend.domain.converter.UserConverter;
import yourssu.backend.domain.dto.request.ArticleRequest;
import yourssu.backend.domain.dto.request.CommentRequest;
import yourssu.backend.domain.dto.response.CommentResponse;
import yourssu.backend.domain.dto.response.TokenDto;
import yourssu.backend.domain.entity.User;
import yourssu.backend.domain.service.CommentService;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.patch;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureRestDocs
@WebMvcTest(controllers = CommentController.class, excludeAutoConfiguration = {SecurityAutoConfiguration.class})
@AutoConfigureMockMvc(addFilters = false)
public class CommentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CommentService commentService;

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
    @DisplayName("articleId, content를 RequestBody로 받아 Comment 객체를 생성한 후 Article과 다대일 관계를 맺는다. 작성자의 이메일, 생성된 Comment의 PK, content를 반환한다.")
    public void createComment() throws Exception {

        // set authentication
        String authenticationToken = setAuthentication("test@mail.com", "user", "1234");

        // request
        CommentRequest.PostCommentDto request = new CommentRequest.PostCommentDto(1L, "comment1");

        // given
        given(commentService.postComment(any(), any()))
                .willReturn(new CommentResponse.CommentDto(1L, "test@mail.com", "comment1"));

        // when&then
        mockMvc.perform(post("/api/v1/comment")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer "+authenticationToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("200"))
                .andExpect(jsonPath("$.result").value("SUCCESS"))
                .andExpect(jsonPath("$.message").value("댓글 작성에 성공했습니다."))
                .andExpect(jsonPath("$.data.commentId").value(1L))
                .andExpect(jsonPath("$.data.email").value("test@mail.com"))
                .andExpect(jsonPath("$.data.content").value("comment1"))
                .andDo(document("post-comment",
                        requestHeaders(
                                headerWithName(HttpHeaders.AUTHORIZATION).description("Bearer token")
                        ),
                        requestFields(
                                fieldWithPath("articleId").description("작성할 Comment와 관계를 맺을 Article의 PK"),
                                fieldWithPath("content").description("작성할 Comment의 본문")
                        ),
                        responseFields(
                                fieldWithPath("code").description("상태 코드"),
                                fieldWithPath("result").description("결과 성공 여부"),
                                fieldWithPath("message").description("결과 메시지"),
                                fieldWithPath("data.commentId").description("생성된 Comment 객체의 PK"),
                                fieldWithPath("data.email").description("해당 Comment를 작성한 사용자의 이메일 주소"),
                                fieldWithPath("data.content").description("생성된 Comment의 본문")
                        )
                ));
    }

    @Test
    public void createCommentWithNonExistArticleId() throws Exception {
        // set authentication
        String authenticationToken = setAuthentication("test@mail.com", "user", "1234");

        // request
        CommentRequest.PostCommentDto request = new CommentRequest.PostCommentDto(30L, "comment2");

        // given
        // 존재하지 않는 article에 대한 id를 requestBody로 제공할 경우, 404 error
        given(commentService.postComment(any(), any()))
                .willThrow(new GeneralException(ErrorStatus.NOT_FOUND_ARTICLE));

        // when&then
        mockMvc.perform(post("/api/v1/comment")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer "+authenticationToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().is4xxClientError())
                .andExpect(jsonPath("$.code").value("404"))
                .andExpect(jsonPath("$.result").value("FAILURE"))
                .andExpect(jsonPath("$.message").value("존재하지 않는 게시글입니다."));
    }

    @Test
    public void postCommentWithInvalidContent() throws Exception {
        // set authentication
        String authenticationToken = setAuthentication("test@mail.com", "user", "1234");

        // request
        // request로 받는 content가 공백일 경우, 400 error
        CommentRequest.PostCommentDto request = new CommentRequest.PostCommentDto(1L, " ");

        // given
        given(commentService.postComment(any(), any()))
                .willThrow(new GeneralException(ErrorStatus.INVALID_CONTENT));

        // when&then
        mockMvc.perform(post("/api/v1/comment")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer "+authenticationToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().is4xxClientError())
                .andExpect(jsonPath("$.code").value("400"))
                .andExpect(jsonPath("$.result").value("FAILURE"))
                .andExpect(jsonPath("$.message").value("유효하지 않은 content 형식입니다."));
    }


    @Test
    @DisplayName("수정할 Comment 객체의 PK를 pathvariable로, content를 RequestBody로 받아 Comment 객체를 수정한 후 작성자의 이메일, 수정된 Comment의 PK, content를 반환한다.")
    public void patchComment() throws Exception {
        // set authentication
        String authenticationToken = setAuthentication("test@mail.com", "user", "1234");

        // request
        CommentRequest.PatchCommentDto request = new CommentRequest.PatchCommentDto("comment3");

        // given
        given(commentService.patchComment(any(), eq(1L), any()))
                .willReturn(new CommentResponse.CommentDto(1L, "test@mail.com", "comment3"));

        // when&then
        mockMvc.perform(patch("/api/v1/comment/{commentId}", 1L)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer "+authenticationToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("200"))
                .andExpect(jsonPath("$.result").value("SUCCESS"))
                .andExpect(jsonPath("$.message").value("댓글 수정에 성공했습니다."))
                .andExpect(jsonPath("$.data.commentId").value(1L))
                .andExpect(jsonPath("$.data.email").value("test@mail.com"))
                .andExpect(jsonPath("$.data.content").value("comment3"))
                .andDo(document("patch-comment",
                        requestHeaders(
                                headerWithName(HttpHeaders.AUTHORIZATION).description("Bearer token")
                        ),
                        pathParameters(
                                parameterWithName("commentId").description("수정할 Comment 객체의 PK")
                        ),
                        requestFields(
                                fieldWithPath("content").description("수정할 Comment의 본문")
                        ),
                        responseFields(
                                fieldWithPath("code").description("상태 코드"),
                                fieldWithPath("result").description("결과 성공 여부"),
                                fieldWithPath("message").description("결과 메시지"),
                                fieldWithPath("data.commentId").description("수정된 Comment 객체의 PK"),
                                fieldWithPath("data.email").description("해당 Comment를 작성한 사용자의 이메일 주소"),
                                fieldWithPath("data.content").description("수정된 Comment의 본문")
                        )
                ));
    }

    @Test
    public void patchCommentWithInvalidContent() throws Exception {
        // set authentication
        String authenticationToken = setAuthentication("test@mail.com", "user", "1234");

        // request
        CommentRequest.PatchCommentDto request = new CommentRequest.PatchCommentDto("comment4");

        // given
        // request로 받는 content가 공백일 경우, 400 error
        given(commentService.patchComment(any(), eq(1L), any()))
                .willThrow(new GeneralException(ErrorStatus.INVALID_CONTENT));

        // when&then
        mockMvc.perform(patch("/api/v1/comment/{commentId}", 1L)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer "+authenticationToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().is4xxClientError())
                .andExpect(jsonPath("$.code").value("400"))
                .andExpect(jsonPath("$.result").value("FAILURE"))
                .andExpect(jsonPath("$.message").value("유효하지 않은 content 형식입니다."));
    }

    @Test
    public void patchCommentWithNonExistCommentId() throws Exception {
        // set authentication
        String authenticationToken = setAuthentication("test@mail.com", "user", "1234");

        // request
        CommentRequest.PatchCommentDto request = new CommentRequest.PatchCommentDto("comment5");

        // given
        // 존재하지 않는 comment에 대한 id를 pathvariable로 제공할 경우, 404 error
        given(commentService.patchComment(any(), eq(30L), any()))
                .willThrow(new GeneralException(ErrorStatus.NOT_FOUND_COMMENT));

        // when&then
        mockMvc.perform(patch("/api/v1/comment/{commentId}", 30L)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer "+authenticationToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().is4xxClientError())
                .andExpect(jsonPath("$.code").value("404"))
                .andExpect(jsonPath("$.result").value("FAILURE"))
                .andExpect(jsonPath("$.message").value("존재하지 않는 댓글입니다."));
    }

    @Test
    public void patchCommentWithUnAuthorizedUser() throws Exception {
        // set authentication
        String authenticationToken = setAuthentication("test@mail.com", "user", "1234");

        // request
        CommentRequest.PatchCommentDto request = new CommentRequest.PatchCommentDto("comment6");

        // given
        // Comment를 작성한 유저 정보와 로그인한 유저 정보가 일치하지 않을 경우, 403 error
        given(commentService.patchComment(any(), eq(1L), any()))
                .willThrow(new GeneralException(ErrorStatus.FORBIDDEN_PATCH_COMMENT));

        // when&then
        mockMvc.perform(patch("/api/v1/comment/{commentId}", 1L)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer "+authenticationToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().is4xxClientError())
                .andExpect(jsonPath("$.code").value("403"))
                .andExpect(jsonPath("$.result").value("FAILURE"))
                .andExpect(jsonPath("$.message").value("댓글 수정 권한이 없습니다."));
    }




}
