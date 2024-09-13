package yourssu.backend.domain.converter;

import yourssu.backend.common.security.TokenDto;

public class TokenConverter {
    public static TokenDto toTokenDto(String accessToken, String refreshToken){
        return TokenDto.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }
}
