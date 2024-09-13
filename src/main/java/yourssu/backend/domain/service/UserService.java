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
import yourssu.backend.common.security.TokenDto;
import yourssu.backend.common.status.ErrorStatus;
import yourssu.backend.domain.converter.UserConverter;
import yourssu.backend.domain.dto.request.UserRequest;
import yourssu.backend.domain.dto.response.UserResponse;
import yourssu.backend.domain.entity.User;
import yourssu.backend.domain.repository.UserRepository;

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
    public void withdrawal(UserRequest.WithDrawalDto request){
        User user = validateUserCredentials(request.getEmail(), request.getPassword());
        userRepository.delete(user);
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

}
