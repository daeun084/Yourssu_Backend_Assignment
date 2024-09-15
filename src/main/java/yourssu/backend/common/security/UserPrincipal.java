package yourssu.backend.common.security;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import yourssu.backend.domain.entity.User;

import java.util.Collection;

@RequiredArgsConstructor
public class UserPrincipal implements UserDetails {
    private final User user;

    public static UserPrincipal create(User user) {
        return new UserPrincipal(user);
    }
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return null;
    }

    public User getUser(){
        return user;
    }

    @Override
    public String getPassword() {
        return user.getPassword();
    }

    @Override
    public String getUsername() {
        return user.getUsername();
    }

    public String getEmail() {
        return user.getEmail();
    }
}
