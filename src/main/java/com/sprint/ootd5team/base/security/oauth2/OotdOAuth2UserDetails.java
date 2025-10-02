package com.sprint.ootd5team.base.security.oauth2;

import com.sprint.ootd5team.domain.user.dto.UserDto;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;

@EqualsAndHashCode(of = "userDto")
@Getter
@RequiredArgsConstructor
public class OotdOAuth2UserDetails implements UserDetails, OAuth2User {

    private final UserDto userDto;
    private final Map<String, Object> attributes;

    @Override
    public Map<String, Object> getAttributes() {
        return attributes;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("Role_"+userDto.role().name()));
    }

    @Override
    public String getPassword() {
        return null;
    }

    @Override
    public String getUsername() {
        return userDto.email();
    }

    @Override
    public String getName() {
        return userDto.name();
    }

    public UUID getUserId() { return userDto.id(); }

}
