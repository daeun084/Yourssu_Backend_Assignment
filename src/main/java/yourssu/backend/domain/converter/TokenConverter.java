package yourssu.backend.domain.converter;

import yourssu.backend.domain.dto.response.TokenDto;

public class TokenConverter {
    public static TokenDto toTokenDto(String accessToken, String refreshToken){
        return TokenDto.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }
}
