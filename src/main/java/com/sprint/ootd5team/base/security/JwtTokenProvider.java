package com.sprint.ootd5team.base.security;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSSigner;
import com.nimbusds.jose.JWSVerifier;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import com.sprint.ootd5team.domain.user.dto.UserDto;
import jakarta.servlet.http.Cookie;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class JwtTokenProvider {

    public static final String REFRESH_TOKEN_COOKIE_NAME = "REFRESH_TOKEN";

    private final int accessTokenExpirationInMs;
    private final int refreshTokenExpirationInMs;

    private final JWSSigner accessTokenSigner;
    private final JWSVerifier accessTokenVerifier;
    private final JWSSigner refreshTokenSigner;
    private final JWSVerifier refreshTokenVerifier;

    /**
     * JwtTokenProvider 생성자로, Signer와 Verifier를 생성한다.
     * @param accessTokenExpirationInMs accessToken 만료기한
     * @param refreshTokenExpirationInMs refreshToken 만료기한
     * @param accessTokenSecret 엑세스토큰 비밀키
     * @param refreshTokenSecret 리프레쉬토큰 비밀키
     * @throws JOSEException 예외발생시
     */
    public JwtTokenProvider(
        @Value("${ootd.jwt.access-token.expiration-ms}") int accessTokenExpirationInMs,
        @Value("${ootd.jwt.refresh-token.expiration-ms}") int refreshTokenExpirationInMs,
        @Value("${ootd.jwt.access-token.secret}")String accessTokenSecret,
        @Value("${ootd.jwt.refresh-token.secret}")String refreshTokenSecret)
        throws JOSEException {
        this.accessTokenExpirationInMs = accessTokenExpirationInMs;
        this.refreshTokenExpirationInMs = refreshTokenExpirationInMs;

        byte[] accessTokenSecretBytes = accessTokenSecret.getBytes(StandardCharsets.UTF_8);
        byte[] refreshTokenSecretBytes = refreshTokenSecret.getBytes(StandardCharsets.UTF_8);

        JWSSigner accessTokenSigner = new MACSigner(accessTokenSecretBytes);
        JWSSigner refreshTokenSigner = new MACSigner(refreshTokenSecretBytes);

        JWSVerifier accessTokenVerifier = new MACVerifier(accessTokenSecretBytes);
        JWSVerifier refreshTokenVerifier = new MACVerifier(refreshTokenSecretBytes);

        this.accessTokenSigner = accessTokenSigner;
        this.accessTokenVerifier = accessTokenVerifier;
        this.refreshTokenSigner = refreshTokenSigner;
        this.refreshTokenVerifier = refreshTokenVerifier;
    }

    /**
     * 토큰 생성 메서드
     * @param userDetails user정보
     * @param expirationMs 만료기한
     * @param signer 서명자
     * @param tokenType 토큰의 타입
     * @return 생성된 Token
     * @throws JOSEException 예외처리
     */
    private String generateToken(
        OotdUserDetails userDetails,
        int expirationMs,
        JWSSigner signer,
        String tokenType) throws JOSEException {

        String tokenId = UUID.randomUUID().toString();
        UserDto user = userDetails.getUserDto();

        Date now = new Date();
        Date expiration = new Date(now.getTime() + expirationMs);

        JWTClaimsSet claimsSet = new JWTClaimsSet.Builder()
            .subject(user.email())
            .jwtID(tokenId)
            .claim("userId", user.id())
            .claim("type", tokenType)
            .claim("role", user.role().name())
            .expirationTime(expiration)
            .build();

        SignedJWT signedJWT = new SignedJWT(
            new JWSHeader(JWSAlgorithm.HS256),
            claimsSet
        );

        signedJWT.sign(signer);
        String token = signedJWT.serialize();

        return token;
    }

    /**
     * 엑세스 토큰 생성
     * @param userDetails user정보
     * @return 생성된 엑세스 토큰
     * @throws JOSEException 예외
     */
    public String generateAccessToken(OotdUserDetails userDetails) throws JOSEException {
        return generateToken(userDetails, accessTokenExpirationInMs, accessTokenSigner, "access");
    }

    /**
     * 리프레쉬 토큰 생성
     * @param userDetails user정보
     * @return 생성된 리프레쉬 토큰
     * @throws JOSEException 예외
     */
    public String generateRefreshToken(OotdUserDetails userDetails) throws JOSEException {
        return generateToken(userDetails, accessTokenExpirationInMs, refreshTokenSigner, "refresh");
    }

    /**
     * 엑세스토큰 검증
     * @param token 검증하길 원하는 엑세스토큰
     * @return 유효하면 true, 아니면 false
     */
    public boolean validateAccessToken(String token){
        return validateToken(token, accessTokenVerifier, "access");
    }

    /**
     * 리프레쉬 토큰 검증
     * @param token 검증하길 원하는 리프레쉬토큰
     * @return 유효하면 true, 아니면 false
     */
    public boolean validateRefreshToken(String token){
        return validateToken(token, refreshTokenVerifier, "refresh");
    }

    /**
     * 토큰 검증 메서드
     * @param token 검증하길 원하는 토큰
     * @param verifier 검증자
     * @param type 토큰의 타입
     * @return 유효하면 true, 아니면 false
     */
    private boolean validateToken(String token, JWSVerifier verifier, String type){
        try {
            SignedJWT signedJWT = SignedJWT.parse(token);

            // verifier signature
            if(!signedJWT.verify(verifier)){
                return false;
            }

            // 토큰의 타입 체크
            if(!signedJWT.getJWTClaimsSet().getClaim("type").toString().equals(type)){
                return false;
            }

            // 만료시간 체크
            Date expirationTime = signedJWT.getJWTClaimsSet().getExpirationTime();
            if (expirationTime == null || expirationTime.before(new Date())) {
                return false;
            }

            // 체크 통과시 true 반환
            return true;

        } catch (Exception e) {
            return false;
        }

    }

    /**
     * Token에서 Email을 추출하는 메서드
     * @param token 추출을 원하는 토큰
     * @return email값
     */
    public String getEmailFromToken(String token) {
        try {
            SignedJWT signedJWT = SignedJWT.parse(token);
            return signedJWT.getJWTClaimsSet().getSubject();
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid JWT token", e);
        }
    }

    /**
     * Token에서 Id를 추출하는 메서드
     * @param token 추출을 원하는 토큰
     * @return 추출된 tokenId
     */
    public String getTokenId(String token) {
        try {
            SignedJWT signedJWT = SignedJWT.parse(token);
            return signedJWT.getJWTClaimsSet().getJWTID();
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid JWT token", e);
        }
    }

    /**
     * 토큰에서 사용자 ID를 추출하는 메서드
     * @param token 추출을 원하는 토큰
     * @return 추출된 UserId
     */
    public UUID getUserId(String token) {
        try {
            SignedJWT signedJWT = SignedJWT.parse(token);
            String userIdStr = (String) signedJWT.getJWTClaimsSet().getClaim("userId");
            if (userIdStr == null) {
                throw new IllegalArgumentException("User ID claim not found in JWT token");
            }
            return UUID.fromString(userIdStr);
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid JWT token", e);
        }
    }

    /**
     * RefreshToken을 쿠키에 저장하여 반환하는 메서드
     * @param refreshToken 리프레쉬 토큰
     * @return 생성된 쿠키
     */
    public Cookie genereateRefreshTokenCookie(String refreshToken) {
        // Set refresh token in HttpOnly cookie
        Cookie refreshCookie = new Cookie(REFRESH_TOKEN_COOKIE_NAME, refreshToken);
        refreshCookie.setHttpOnly(true);
        refreshCookie.setSecure(true); // Use HTTPS in production
        refreshCookie.setPath("/");
        refreshCookie.setMaxAge(refreshTokenExpirationInMs / 1000);
        return refreshCookie;
    }

    /**
     * RefreshToken을 강제로 만료시키는 쿠키를 생성하는 메서드 (로그아웃에서 사용)
     * @return 생성된 쿠키
     */
    public Cookie genereateRefreshTokenExpirationCookie() {
        Cookie refreshCookie = new Cookie(REFRESH_TOKEN_COOKIE_NAME, "");
        refreshCookie.setHttpOnly(true);
        refreshCookie.setSecure(true); // Use HTTPS in production
        refreshCookie.setPath("/");
        refreshCookie.setMaxAge(0);
        return refreshCookie;
    }

}
