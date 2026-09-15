package com.mindcup.backend.domain.user.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userId;

    @Column(nullable = false, unique = true)
    private String email;

    private String password;

    @Column(nullable = false)
    private String nickname;

    @Column(nullable = false)
    private String provider; // LOCAL, GOOGLE

    private String providerId;

    @Column(nullable = false, unique = true)
    private String friendCode;

    @Column(nullable = false)
    private boolean isStatusPublic;

    @Column(nullable = false)
    private String languageSetting; // KO, EN

    @Column(nullable = false)
    private String unlockedWorldLevel; // SMALL_CUP, LARGE_CUP ...

    private String refreshToken;

    @Builder
    public User(String email, String password, String nickname, String provider, String providerId,
                String friendCode, boolean isStatusPublic, String languageSetting, String unlockedWorldLevel) {
        this.email = email;
        this.password = password;
        this.nickname = nickname;
        this.provider = provider;
        this.providerId = providerId;
        this.friendCode = friendCode;
        this.isStatusPublic = isStatusPublic;
        this.languageSetting = languageSetting;
        this.unlockedWorldLevel = unlockedWorldLevel;
    }

    public void updateRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public void clearRefreshToken() {
        this.refreshToken = null;
    }

    public void updateLanguageSetting(String languageSetting) {
        this.languageSetting = languageSetting;
    }

    public void updateUnlockedWorldLevel(String unlockedWorldLevel) {
        this.unlockedWorldLevel = unlockedWorldLevel;
    }
}
