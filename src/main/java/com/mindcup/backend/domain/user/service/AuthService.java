package com.mindcup.backend.domain.user.service;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.mindcup.backend.domain.user.dto.GoogleLoginRequest;
import com.mindcup.backend.domain.user.dto.LoginRequest;
import com.mindcup.backend.domain.user.dto.SignupRequest;
import com.mindcup.backend.domain.user.dto.TokenResponse;
import com.mindcup.backend.domain.user.dto.UserResponse;
import com.mindcup.backend.domain.user.entity.User;
import com.mindcup.backend.domain.user.repository.UserRepository;
import com.mindcup.backend.global.exception.BusinessException;
import com.mindcup.backend.global.exception.ErrorCode;
import com.mindcup.backend.global.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider tokenProvider;

    @Value("${google.client-id}")
    private String googleClientId;

    @Transactional
    public UserResponse signup(SignupRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BusinessException(ErrorCode.EMAIL_DUPLICATE);
        }

        String friendCode = generateUniqueFriendCode();
        User user = User.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .nickname(request.getNickname())
                .provider("LOCAL")
                .friendCode(friendCode)
                .isStatusPublic(true)
                .languageSetting("KO")
                .unlockedWorldLevel("SMALL_CUP")
                .build();

        userRepository.save(user);
        return UserResponse.from(user);
    }

    @Transactional
    public TokenResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_CREDENTIALS));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BusinessException(ErrorCode.INVALID_CREDENTIALS);
        }

        return issueTokens(user, false);
    }

    @Transactional
    public TokenResponse googleLogin(GoogleLoginRequest request) {
        String idTokenString = request.getIdToken();
        String email;
        String name;
        String providerId;

        // Mock 구글 로그인 처리 (로컬 개발 및 가상기기 검증용)
        if (idTokenString.startsWith("mock_google_id_token")) {
            email = idTokenString.replace("mock_google_id_token_", "") + "@google-mock.com";
            name = "구글테스터_" + email.split("@")[0];
            providerId = "GOOGLE_" + email.hashCode();
        } else {
            // 실제 구글 API 인증 검증
            GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(new NetHttpTransport(), new GsonFactory())
                    .setAudience(Collections.singletonList(googleClientId))
                    .build();

            try {
                GoogleIdToken idToken = verifier.verify(idTokenString);
                if (idToken != null) {
                    GoogleIdToken.Payload payload = idToken.getPayload();
                    email = payload.getEmail();
                    name = (String) payload.get("name");
                    providerId = payload.getSubject();
                } else {
                    throw new BusinessException(ErrorCode.GOOGLE_LOGIN_FAILED);
                }
            } catch (GeneralSecurityException | IOException e) {
                log.error("Google ID Token validation failed", e);
                throw new BusinessException(ErrorCode.GOOGLE_LOGIN_FAILED);
            }
        }

        boolean isNewUser = false;
        User user = userRepository.findByEmail(email).orElse(null);

        if (user == null) {
            isNewUser = true;
            String friendCode = generateUniqueFriendCode();
            user = User.builder()
                    .email(email)
                    .password(null) // 소셜 로그인은 비밀번호 없음
                    .nickname(name != null ? name : "마음친구")
                    .provider("GOOGLE")
                    .providerId(providerId)
                    .friendCode(friendCode)
                    .isStatusPublic(true)
                    .languageSetting("KO")
                    .unlockedWorldLevel("SMALL_CUP")
                    .build();
            userRepository.save(user);
        }

        return issueTokens(user, isNewUser);
    }

    @Transactional
    public TokenResponse refresh(String refreshToken) {
        User user = userRepository.findByRefreshToken(refreshToken)
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_TOKEN));

        // 토큰 유효성 검증
        try {
            tokenProvider.validateToken(refreshToken);
        } catch (Exception e) {
            // 토큰 만료 등 검증 실패 시 DB 토큰 정리 후 예외 처리
            user.clearRefreshToken();
            throw new BusinessException(ErrorCode.INVALID_TOKEN);
        }

        return issueTokens(user, false);
    }

    @Transactional
    public void logout(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND));
        user.clearRefreshToken();
    }

    private TokenResponse issueTokens(User user, boolean isNewUser) {
        String accessToken = tokenProvider.generateAccessToken(user.getUserId());
        String refreshToken = tokenProvider.generateRefreshToken(user.getUserId());

        user.updateRefreshToken(refreshToken);

        return new TokenResponse(accessToken, refreshToken, 3600L, isNewUser);
    }

    private String generateUniqueFriendCode() {
        String code;
        do {
            String uuid = UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase();
            code = "MIND-" + uuid.substring(0, 4) + "-" + uuid.substring(4, 8);
        } while (userRepository.existsByFriendCode(code));
        return code;
    }
}
