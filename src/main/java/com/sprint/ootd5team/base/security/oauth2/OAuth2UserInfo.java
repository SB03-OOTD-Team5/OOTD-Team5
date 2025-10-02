package com.sprint.ootd5team.base.security.oauth2;

public interface OAuth2UserInfo {
    String getProvider();
    String getProviderId();
    String getName();
    String getEmail();
}
