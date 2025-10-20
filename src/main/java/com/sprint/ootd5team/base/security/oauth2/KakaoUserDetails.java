package com.sprint.ootd5team.base.security.oauth2;

import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;

@AllArgsConstructor
@Slf4j
public class KakaoUserDetails implements OAuth2UserInfo{

    private Map<String, Object> attributes;

    @Override
    public String getProvider() {
        return "kakao";
    }

    @Override
    public String getProviderId() {
        return String.valueOf(attributes.get("id"));
    }

    @Override
    public String getName() {
        Map<String, Object> kakaoAccount = (Map<String, Object>) attributes.get("kakao_account");

        if (kakaoAccount == null) {
            log.error("kakao_account not found");
            throw new OAuth2AuthenticationException("카카오 계정 정보가 없습니다.");
        }

        Map<String, Object> profile = (Map<String, Object>) kakaoAccount.get("profile");
        if (profile == null || profile.get("nickname") == null) {
            log.error("Kakao nickname not found");
            throw new OAuth2AuthenticationException("카카오 닉네임이 없습니다. 프로필 동의항목을 확인하세요.");
        }

        return (String) profile.get("nickname");
    }

    @Override
    public String getEmail() {
        return getName()+"_"+getProviderId()+"@kakao.com";
    }
}
