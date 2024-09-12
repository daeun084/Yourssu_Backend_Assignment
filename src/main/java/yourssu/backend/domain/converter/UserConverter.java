package yourssu.backend.domain.converter;

import yourssu.backend.domain.dto.response.UserResponse;
import yourssu.backend.domain.entity.User;

public class UserConverter {

    public static User toUser(String email, String username, String password){
        return User.builder()
                .email(email)
                .username(username)
                .password(password)
                .build();
    }

    public static UserResponse.UserDto toUserDto(User user){
        return UserResponse.UserDto.builder()
                .email(user.getEmail())
                .username(user.getUsername())
                .build();
    }
}
