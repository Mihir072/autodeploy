package com.autodeploy.auth.security;

import com.autodeploy.auth.entity.User;
import com.autodeploy.auth.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * Custom OAuth2 user service that:
 * <ol>
 *   <li>Calls GitHub's {@code /user} endpoint via the default implementation</li>
 *   <li>Extracts GitHub profile data (id, login, email, avatar_url)</li>
 *   <li>Creates or updates the platform {@link User} record in auth_db</li>
 *   <li>Wraps the result in {@link CustomOAuth2User} so the success handler has
 *       direct access to the persisted entity</li>
 * </ol>
 *
 * <p>Note: The GitHub OAuth2 access token is NOT stored here — it is captured
 * in {@link OAuth2AuthenticationSuccessHandler} from the {@code OAuth2AuthorizedClient}
 * after this method returns.</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final AuthService authService;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);
        try {
            return processGitHubUser(oAuth2User);
        } catch (Exception ex) {
            log.error("Failed to process GitHub OAuth2 user: {}", ex.getMessage(), ex);
            throw new OAuth2AuthenticationException(
                    new OAuth2Error("user_processing_error",
                                   "Failed to create or update user: " + ex.getMessage(), null));
        }
    }

    private OAuth2User processGitHubUser(OAuth2User oAuth2User) {
        Map<String, Object> attrs = oAuth2User.getAttributes();

        Long githubId = extractLong(attrs, "id");
        String username = (String) attrs.get("login");
        String email    = (String) attrs.get("email");     // may be null for private accounts
        String avatar   = (String) attrs.get("avatar_url");

        if (githubId == null || username == null) {
            throw new IllegalStateException(
                "GitHub OAuth2 response missing required fields (id, login)");
        }

        log.debug("Processing GitHub OAuth2 login for user: {} (githubId={})", username, githubId);
        User user = authService.findOrCreateUser(githubId, username, email, avatar);

        return new CustomOAuth2User(oAuth2User, user);
    }

    private Long extractLong(Map<String, Object> attrs, String key) {
        Object val = attrs.get(key);
        if (val instanceof Number n) return n.longValue();
        if (val instanceof String s) return Long.parseLong(s);
        return null;
    }
}
