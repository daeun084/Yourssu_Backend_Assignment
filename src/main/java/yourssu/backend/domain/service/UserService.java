package yourssu.backend.domain.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import yourssu.backend.common.exception.GeneralException;
import yourssu.backend.common.security.JwtTokenProvider;
import yourssu.backend.common.security.UserPrincipal;
import yourssu.backend.domain.dto.response.TokenDto;
import yourssu.backend.common.status.ErrorStatus;
import yourssu.backend.domain.converter.UserConverter;
import yourssu.backend.domain.dto.request.UserRequest;
import yourssu.backend.domain.dto.response.UserResponse;
import yourssu.backend.domain.entity.Comment;
import yourssu.backend.domain.entity.User;
import yourssu.backend.domain.repository.UserRepository;

import java.util.Objects;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final AuthenticationManager authenticationManager;

    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[a-zA-Z0-9+-_.]+@[a-zA-Z0-9-]+\\.[a-zA-Z0-9-.]+$");


    /*
     * 이름, 메일, 비밀번호를 받아 회원가입 후 이름과 메일을 반환
     * @param request
     * @return
     */
    @Transactional
    public UserResponse.UserDto signUp(UserRequest.SignUpDto request){
        String username = request.getUsername();
        String email = request.getEmail();
        String password = passwordEncoder.encode(request.getPassword());

        validateEmailPattern(email);
        checkDuplicateEmail(email);
        checkDuplicateUsername(username);

        User user = UserConverter.toUser(email, username, password);
        userRepository.save(user);

        return UserConverter.toUserDto(user);
    }

    /*
     * 이메일, 비밀번호를 받아 로그인 후 토큰 반환
     * @param request
     * @return
     */
    @Transactional
    public TokenDto signIn(UserRequest.SignInDto request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );
        return jwtTokenProvider.createToken(authentication);
    }

    /*
     * 메일, 전화번호를 받아 유저 삭제
     * @param request
     */
    @Transactional
    public void withdrawal(UserRequest.WithDrawalDto request, UserPrincipal userprincipal){
        User user = userprincipal.getUser();
        User targetUser = validateUserCredentials(request.getEmail(), request.getPassword());
        validateIsUserAuthorized(user, targetUser);

        userRepository.delete(targetUser);
    }

    private void validateEmailPattern(String email) {
        if (!EMAIL_PATTERN.matcher(email).matches()){
            throw new GeneralException(ErrorStatus.INVALID_EMAIL);
        }
    }

    private void checkDuplicateEmail(String email) {
        if (userRepository.existsUserByEmail(email)){
            throw new GeneralException(ErrorStatus.DUPLICATE_EMAIL);
        }
    }

    private void checkDuplicateUsername(String username) {
        if (userRepository.existsUserByUsername(username)){
            throw new GeneralException(ErrorStatus.DUPLICATE_USERNAME);
        }
    }

    public User validateUserCredentials(String email, String password) {
        User user = findUserByEmail(email);

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new GeneralException(ErrorStatus.NOT_MATCH_PASSWORD);
        }
        return user;
    }

    public void validateIsUserAuthorized(User user, User targetUser) {
        if (!Objects.equals(user.getUserId(), targetUser.getUserId()))
            throw new GeneralException(ErrorStatus.FORBIDDEN_WITHDRAWAL);
    }

    private User findUserByEmail(String email){
        return userRepository.findUserByEmail(email)
                .orElseThrow(() -> new GeneralException(ErrorStatus.NOT_FOUND_USER));
    }

}
