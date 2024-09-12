package yourssu.backend.domain.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import yourssu.backend.common.exception.GeneralException;
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

    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[a-zA-Z0-9+-\\_.]+@[a-zA-Z0-9-]+\\.[a-zA-Z0-9-.]+$");


    /*
     * 이름, 메일, 비밀번호를 받아 회원가입 후 이름과 메일을 반환
     * @param request
     * @return
     */
    @Transactional
    public UserResponse.UserDto signUp(UserRequest.SignUpDto request){
        String username = request.getUsername();
        String email = request.getEmail();
        validateEmail(email);
        String password = passwordEncoder.encode(request.getPassword());

        User user = UserConverter.toUser(email, username, password);
        userRepository.save(user);

        return UserConverter.toUserDto(user);
    }

    private void validateEmail(String email) {
        if (!EMAIL_PATTERN.matcher(email).matches()){
            throw new GeneralException(ErrorStatus.INVALID_EMAIL);
        }
    }

}
