package yourssu.backend.common.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import yourssu.backend.domain.converter.TokenConverter;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.stream.Collectors;


@Component
@RequiredArgsConstructor
public class JwtTokenProvider {
    @Value("${jwt.access-expiration-ms}")
    private long ACCESS_EXPIRATION_MS;
    @Value("${jwt.refresh-expiration-ms}")
    private long REFRESH_EXPIRATION_MS;
    @Value("${jwt.secret-key}")
    private String secretKey;
    private SecretKey key;
    private final CustomUserDetailService userDetailService;

    @Transactional
    public TokenDto createToken(Authentication authentication){
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        System.out.println("userDetail"+userDetails.getUsername()+"/"+userDetails.getPassword());
        String authorities = getAuthorities(authentication);

        String username = userDetails.getUsername();

        Long now = System.currentTimeMillis();
        String accessToken = createAccessToken(username, authorities, now);
        String refreshToken = createRefreshToken(now);

        return TokenConverter.toTokenDto(accessToken, refreshToken);
    }

    public boolean validateToken(String token){
        try{
            getClaims(token);
            return true;
        } catch (UnsupportedJwtException | MalformedJwtException | ExpiredJwtException e) {
            throw new JwtException("Validate Access Token Exception");
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("IllegalArgumentException");
        }
    }

    public Authentication getAuthentication(String accessToken) {
        Claims claims = getClaims(accessToken);
        String username = claims.get("username").toString();
        UserDetails userDetails = userDetailService.loadUserByName(username);
        return new UsernamePasswordAuthenticationToken(userDetails, "", userDetails.getAuthorities());
    }

    private String getAuthorities(Authentication authentication){
        return authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(","));
    }

    private Claims getClaims(String token){
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (ExpiredJwtException e) {
            return e.getClaims();
        }
    }

    private String createAccessToken(String username, String authorities, Long now){
        return Jwts.builder()
                .setSubject("access-token")
                .claim("username", username)
                .claim("auth", authorities)
                .setIssuedAt(new Date(now))
                .setExpiration(new Date(now + ACCESS_EXPIRATION_MS))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    private String createRefreshToken(Long now){
        return Jwts.builder()
                .setSubject("refresh-token")
                .setIssuedAt(new Date(now))
                .setExpiration(new Date(now + REFRESH_EXPIRATION_MS))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    private SecretKey getSigningKey() {
        if (key == null) {
            synchronized (this) {
                if (key == null) {
                    key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secretKey));
                }
            }
        }
        return key;
    }

}



