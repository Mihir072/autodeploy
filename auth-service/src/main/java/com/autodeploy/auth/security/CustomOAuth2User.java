package com.autodeploy.auth.security;

import com.autodeploy.auth.entity.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Wrapper around Spring's {@link OAuth2User} that also carries the persisted
 * {@link User} entity, making it available in the OAuth2 success handler.
 *
 * <p>All attribute/authority/name calls are delegated to the original {@link OAuth2User}.</p>
 */
public class CustomOAuth2User implements OAuth2User {

    private final OAuth2User delegate;
    private final User user;

    public CustomOAuth2User(OAuth2User delegate, User user) {
        this.delegate = delegate;
        this.user = user;
    }

    /** The persisted platform user. Available in success handler to issue JWT. */
    public User getUser() {
        return user;
    }

    // ─── OAuth2User delegation ────────────────────────────────────────────────

    @Override
    public Map<String, Object> getAttributes() {
        return delegate.getAttributes();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_USER"));
    }

    /**
     * Returns the GitHub login name (the user-name-attribute configured as "id" in YAML
     * is the numeric GitHub user ID — we override this to return the username for readability).
     */
    @Override
    public String getName() {
        return user.getUsername();
    }
}
