package yourssu.backend.common.security;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import yourssu.backend.common.exception.GeneralException;
import yourssu.backend.common.status.ErrorStatus;
import yourssu.backend.domain.entity.User;
import yourssu.backend.domain.repository.UserRepository;

@Service
@RequiredArgsConstructor
public class CustomUserDetailService implements UserDetailsService {
    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = findUserByEmail(email);
        return UserPrincipal.create(user);
    }

    public UserDetails loadUserByName(String username) throws UsernameNotFoundException {
        User user = findUserByUsername(username);
        return UserPrincipal.create(user);
    }

    private User findUserByEmail(String email){
        return userRepository.findUserByEmail(email)
                .orElseThrow(() -> new GeneralException(ErrorStatus.NOT_FOUND_USER));
    }

    private User findUserByUsername(String username) {
        return userRepository.findUserByUsername(username)
                .orElseThrow(() -> new GeneralException(ErrorStatus.NOT_FOUND_USER));
    }
}
