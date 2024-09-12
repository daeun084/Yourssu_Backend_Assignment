package yourssu.backend.domain.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

public class UserRequest {
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SignUpDto {
        @NotBlank
        private String email;
        @NotBlank
        private String password;
        @NotBlank
        private String username;
    }
}
