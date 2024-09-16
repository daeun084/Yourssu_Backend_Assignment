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
import yourssu.backend.domain.controller.UserController;
import yourssu.backend.domain.converter.UserConverter;
import yourssu.backend.domain.dto.request.UserRequest;
import yourssu.backend.domain.dto.response.TokenDto;
import yourssu.backend.domain.dto.response.UserResponse;
import yourssu.backend.domain.entity.User;
import yourssu.backend.domain.service.UserService;


import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.Mockito.doThrow;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureRestDocs
@WebMvcTest(controllers = UserController.class, excludeAutoConfiguration = {SecurityAutoConfiguration.class})
@AutoConfigureMockMvc(addFilters = false) // 인증인가 필터 제외
public class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    @MockBean
    private JpaMetamodelMappingContext jpaMappingContext;

    @MockBean
    private CustomUserDetailService customUserDetailService;


    @Test
    @DisplayName("username, email, password를 RequestBody로 받아 유저 객체를 생성한다. email, username은 중복을 허용하지 않는다.")
    public void createUser() throws Exception {
        UserRequest.SignUpDto request = new UserRequest.SignUpDto("test@mail.com", "1234", "user1");

        // given
        given(userService.signUp(any()))
                .willReturn(new UserResponse.UserDto("test@mail.com", "user1"));

        // when&then
        mockMvc.perform(post("/api/v1/sign-up")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("200"))
                .andExpect(jsonPath("$.result").value("SUCCESS"))
                .andExpect(jsonPath("$.message").value("회원가입에 성공했습니다."))
                .andExpect(jsonPath("$.data.email").value("test@mail.com"))
                .andExpect(jsonPath("$.data.username").value("user1"))
                .andDo(document("post-sign-up",
                        requestFields(
                                fieldWithPath("email").description("이메일 주소"),
                                fieldWithPath("password").description("비밀번호"),
                                fieldWithPath("username").description("이름")
                        ),
                        responseFields(
                                fieldWithPath("code").description("상태 코드"),
                                fieldWithPath("result").description("결과 성공 여부"),
                                fieldWithPath("message").description("결과 메시지"),
                                fieldWithPath("data.email").description("이메일"),
                                fieldWithPath("data.username").description("이름")
                        )
                ));
    }

    @Test
    public void createUserWithInvalidEmail() throws Exception {
        UserRequest.SignUpDto request = new UserRequest.SignUpDto("test@com", "1234", "user2");

        // given
        given(userService.signUp(any()))
                .willThrow(new GeneralException(ErrorStatus.INVALID_EMAIL));

        // when&then
        mockMvc.perform(post("/api/v1/sign-up")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().is4xxClientError())
                .andExpect(jsonPath("$.code").value("400"))
                .andExpect(jsonPath("$.result").value("FAILURE"))
                .andExpect(jsonPath("$.message").value("이메일 형식이 올바르지 않습니다."));
    }

    @Test
    public void createUserWithDuplicatedEmail() throws Exception {
        UserRequest.SignUpDto request = new UserRequest.SignUpDto("test@gmail.com", "1234", "user3");

        // given
        given(userService.signUp(any()))
                .willThrow(new GeneralException(ErrorStatus.DUPLICATE_EMAIL));

        // when&then
        mockMvc.perform(post("/api/v1/sign-up")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().is4xxClientError())
                .andExpect(jsonPath("$.code").value("400"))
                .andExpect(jsonPath("$.result").value("FAILURE"))
                .andExpect(jsonPath("$.message").value("해당 이메일로 생성된 계정이 이미 존재합니다."));
    }

    @Test
    public void createUserWithDuplicatedName() throws Exception {
        UserRequest.SignUpDto request = new UserRequest.SignUpDto("test2@gmail.com", "1234", "user1");

        // given
        given(userService.signUp(any()))
                .willThrow(new GeneralException(ErrorStatus.DUPLICATE_USERNAME));

        // when&then
        mockMvc.perform(post("/api/v1/sign-up")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().is4xxClientError())
                .andExpect(jsonPath("$.code").value("400"))
                .andExpect(jsonPath("$.result").value("FAILURE"))
                .andExpect(jsonPath("$.message").value("해당 유저 이름이 이미 존재합니다."));
    }

    @Test
    @DisplayName("email, password를 RequestBody로 받아 로그인한다.")
    public void signIn() throws Exception {
        UserRequest.SignInDto request = new UserRequest.SignInDto("test@mail.com", "1234");

        // given
        given(userService.signIn(any()))
                .willReturn(new TokenDto("accessToken", "refreshToken"));

        // when&then
        mockMvc.perform(post("/api/v1/sign-in")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("200"))
                .andExpect(jsonPath("$.result").value("SUCCESS"))
                .andExpect(jsonPath("$.message").value("로그인에 성공했습니다."))
                .andExpect(jsonPath("$.data.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.data.refreshToken").isNotEmpty())
                .andDo(document("post-sign-in",
                        requestFields(
                                fieldWithPath("email").description("이메일 주소"),
                                fieldWithPath("password").description("비밀번호")
                        ),
                        responseFields(
                                fieldWithPath("code").description("상태 코드"),
                                fieldWithPath("result").description("결과 성공 여부"),
                                fieldWithPath("message").description("결과 메시지"),
                                fieldWithPath("data.accessToken").description("인증된 사용자의 accessToken"),
                                fieldWithPath("data.refreshToken").description("인증된 사용자의 refreshToken")
                        )
                ));
    }

    @Test
    @DisplayName("email, password를 RequestBody로 받아 해당 유저 객체를 삭제")
    public void withdrawal() throws Exception {
        UserRequest.SignInDto request = new UserRequest.SignInDto("test@mail.com", "1234");

        // signIn mocking
        TokenDto tokenDto = new TokenDto("access-token", "refresh-token");
        given(userService.signIn(any(UserRequest.SignInDto.class))).willReturn(tokenDto);

        String token = userService.signIn(request).getAccessToken();
        User targetUser = UserConverter.toUser("test@mail.com", "user1", "1234");

        // given
        given(userService.validateUserCredentials(anyString(), anyString())).willReturn(targetUser);
        willDoNothing().given(userService).validateIsUserAuthorized(any(User.class), any(User.class));

        // when&then
        mockMvc.perform(delete("/api/v1/withdrawal")
                        .header("Authorization", ("Bearer " + token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("200"))
                .andExpect(jsonPath("$.result").value("SUCCESS"))
                .andExpect(jsonPath("$.message").value("회원탈퇴에 성공했습니다."))
                .andDo(document("delete-withdrawal",
                        requestFields(
                                fieldWithPath("email").description("이메일 주소"),
                                fieldWithPath("password").description("비밀번호")
                        ),
                        responseFields(
                                fieldWithPath("code").description("상태 코드"),
                                fieldWithPath("result").description("결과 성공 여부"),
                                fieldWithPath("message").description("결과 메시지")
                        )
                ));
    }

    @Test
    public void withdrawalWithInvalidUser() throws Exception {
        UserRequest.WithDrawalDto withdrawalRequest = new UserRequest.WithDrawalDto("test@mail.com", "1234");

        // 1. 모킹된 UserDetails 설정
        User user = UserConverter.toUser("test2@mail.com", "user2", "1234");
        UserDetails userDetails = new UserPrincipal(user);

        // 2. customUserDetailService 모킹 설정
        given(customUserDetailService.loadUserByUsername("test2@mail.com"))
                .willReturn(userDetails);

        // 3. Authentication 객체 생성 및 SecurityContext에 설정
        UsernamePasswordAuthenticationToken authenticationToken =
                new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authenticationToken);

        // given
        doThrow(new GeneralException(ErrorStatus.FORBIDDEN_WITHDRAWAL))
                .when(userService).withdrawal(any(UserRequest.WithDrawalDto.class), any(UserPrincipal.class));

        // when&then
        mockMvc.perform(delete("/api/v1/withdrawal")
                        .with(authentication(authenticationToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(withdrawalRequest)))
                .andExpect(status().is4xxClientError())
                .andExpect(jsonPath("$.code").value("403"))
                .andExpect(jsonPath("$.result").value("FAILURE"))
                .andExpect(jsonPath("$.message").value("계정탈퇴 권한이 없습니다."));
    }




}
