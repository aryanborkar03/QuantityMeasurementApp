package com.app.quantitymeasurement.security.oauth2;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.app.quantitymeasurement.entity.User;
import com.app.quantitymeasurement.enums.AuthProvider;
import com.app.quantitymeasurement.enums.Role;
import com.app.quantitymeasurement.repository.UserRepository;
import com.app.quantitymeasurement.security.UserPrincipal;

import lombok.extern.slf4j.Slf4j;


@Slf4j
@Service
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

	private UserRepository userRepository;
	
	public CustomOAuth2UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

	/**
	 * Called by Spring Security after it has exchanged the OAuth2 authorization
	 * code for an access token and fetched the user's profile from the provider's
	 * UserInfo endpoint.
	 *
	 * <p>
	 * The {@code userRequest.getClientRegistration().getRegistrationId()} value is
	 * {@code "google"} or {@code "github"}, which this service uses to apply
	 * provider-specific attribute extraction logic.
	 * </p>
	 *
	 * @param userRequest contains the registered client, the access token, and
	 *                    additional request parameters
	 * @return a {@link UserPrincipal} wrapping the persisted/updated user entity
	 * @throws OAuth2AuthenticationException if the email is missing or a provider
	 *                                       conflict is detected
	 */
	@Override
	@Transactional
	public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
		OAuth2User oAuth2User = super.loadUser(userRequest);

		try {
			return processOAuth2User(userRequest, oAuth2User);
		} catch (OAuth2AuthenticationException ex) {
			throw ex;
		} catch (Exception ex) {
			log.error("Unexpected error processing OAuth2 user: {}", ex.getMessage());
			throw new InternalAuthenticationServiceException(ex.getMessage(), ex);
		}
	}

	// -------------------------------------------------------------------------
	// Core dispatch
	// -------------------------------------------------------------------------

	/**
	 * Dispatches to the provider-specific extraction method based on the
	 * registration ID ({@code "google"} or {@code "github"}), then resolves or
	 * creates the local {@link User} record.
	 *
	 * @param userRequest the OAuth2 user request supplying the registration ID
	 * @param oAuth2User  the raw OAuth2 user returned by the provider
	 * @return a {@link UserPrincipal} for the resolved user
	 */
	private UserPrincipal processOAuth2User(OAuth2UserRequest userRequest, OAuth2User oAuth2User) {
		String registrationId = userRequest.getClientRegistration().getRegistrationId();
		Map<String, Object> attributes = oAuth2User.getAttributes();

		/*
		 * Extract email, name, imageUrl, and providerId using the attribute map
		 * structure specific to each provider.
		 */
		OAuthUserInfo info = extractUserInfo(registrationId, attributes);

		if (!StringUtils.hasText(info.email)) {
			throw new OAuth2AuthenticationException(new OAuth2Error("invalid_request",
				"Email not found in " 
					+ registrationId 
					+ " OAuth2 response. "
					+ "For GitHub, ensure your email is public.",
				null));
		}

		return userRepository.findByEmail(info.email).map(existing -> updateExistingUser(existing, info, attributes))
			.orElseGet(() -> registerNewUser(info, attributes));
	}

	// -------------------------------------------------------------------------
	// Provider-specific attribute extraction
	// -------------------------------------------------------------------------

	/**
	 * Extracts a normalised {@link OAuthUserInfo} from the raw attribute map
	 * according to the attribute keys each provider uses.
	 *
	 * <p>
	 * <b>Google attribute keys:</b> {@code email}, {@code name}, {@code picture},
	 * {@code sub}.
	 * </p>
	 *
	 * <p>
	 * <b>GitHub attribute keys:</b> {@code email} (may be null if private),
	 * {@code name} (may be null — fallback to {@code login}), {@code avatar_url},
	 * {@code id} (numeric, cast to String).
	 * </p>
	 *
	 * @param registrationId {@code "google"} or {@code "github"}
	 * @param attributes     raw attribute map from the provider's UserInfo endpoint
	 * @return normalised user info container
	 * @throws OAuth2AuthenticationException if the registration ID is not supported
	 */
	private OAuthUserInfo extractUserInfo(String registrationId, Map<String, Object> attributes) {
		switch (registrationId) {
		case "google":
			return new OAuthUserInfo((String) attributes.get("email"), (String) attributes.get("name"),
					(String) attributes.get("picture"), (String) attributes.get("sub"), // stable Google subject ID
					AuthProvider.GOOGLE);

		case "github":
			/*
			 * GitHub returns the user's primary email only when the user:email scope is
			 * granted AND the email is not private. The numeric 'id' field is the stable
			 * identifier (users can change their login username, but the ID is immutable).
			 */
			String githubName = (String) attributes.get("name");
			if (!StringUtils.hasText(githubName)) {
				// 'login' is the GitHub username — always present
				githubName = (String) attributes.get("login");
			}
			String githubId = String.valueOf(attributes.get("id"));

			return new OAuthUserInfo((String) attributes.get("email"), githubName,
					(String) attributes.get("avatar_url"), githubId, AuthProvider.GITHUB);

		default:
			throw new OAuth2AuthenticationException(
				new OAuth2Error(
					"unsupported_provider",
					"Unsupported OAuth2 provider: " 
						+ registrationId,
					null));
		}
	}
	// -------------------------------------------------------------------------
	// User resolution
	// -------------------------------------------------------------------------

	/**
	 * Handles a returning OAuth2 user. Refreshes mutable profile fields (name and
	 * image URL) and guards against cross-provider account takeover.
	 *
	 * <p>
	 * Conflict rules:
	 * </p>
	 * <ul>
	 * <li>LOCAL account → reject with a descriptive message.</li>
	 * <li>Same provider → update and return.</li>
	 * <li>Different OAuth2 provider (e.g. GOOGLE account logging in via GITHUB) →
	 * reject so the user understands which provider to use.</li>
	 * </ul>
	 *
	 * @param existing   the persisted user entity found by email
	 * @param info       normalised provider attributes
	 * @param attributes raw attribute map (forwarded to UserPrincipal for
	 *                   OAuth2User)
	 * @return a {@link UserPrincipal} wrapping the updated entity
	 * @throws OAuth2AuthenticationException on provider conflict
	 */
	private UserPrincipal updateExistingUser(User existing, OAuthUserInfo info, Map<String, Object> attributes) {
		if (existing.getProvider() == AuthProvider.LOCAL) {
			throw new OAuth2AuthenticationException(
				new OAuth2Error(
					"account_conflict",
					"Account already exists with email " 
						+ existing.getEmail()
						+ ". Please login using email and password.",
					null));
		}

		if (existing.getProvider() != info.provider) {
			throw new OAuth2AuthenticationException(
				new OAuth2Error(
					"account_conflict",
					"Account already registered with " 
						+ existing.getProvider().name().toLowerCase()
						+ ". Please login using " 
						+ existing.getProvider().name().toLowerCase(),
					null));
		}

		existing.setName(info.name);
		existing.setImageUrl(info.imageUrl);
		User saved = userRepository.save(existing);

		log.info("Updated existing {} user: {}", info.provider, saved.getEmail());
		return UserPrincipal.create(saved, attributes);
	}

	/**
	 * Handles a first-time OAuth2 sign-in. Creates and persists a new {@link User}
	 * with the resolved provider, role {@link Role#USER}, and profile data
	 * extracted from the provider's attribute map.
	 *
	 * @param info       normalised provider attributes
	 * @param attributes raw attribute map (forwarded to UserPrincipal for
	 *                   OAuth2User)
	 * @return a {@link UserPrincipal} wrapping the newly persisted entity
	 */
	private UserPrincipal registerNewUser(OAuthUserInfo info, Map<String, Object> attributes) {
		User newUser = User.builder().email(info.email).name(info.name).imageUrl(info.imageUrl).provider(info.provider)
				.providerId(info.providerId).role(Role.USER).password(null).build();

		User saved = userRepository.save(newUser);
		log.info("Registered new {} user: {}", info.provider, saved.getEmail());
		return UserPrincipal.create(saved, attributes);
	}

	// -------------------------------------------------------------------------
	// Internal value object
	// -------------------------------------------------------------------------

	/**
	 * Normalised container for user attributes extracted from any OAuth2 provider.
	 * Decouples the provider-specific attribute keys from the user resolution
	 * logic.
	 */
	private static class OAuthUserInfo {
		final String email;
		final String name;
		final String imageUrl;
		final String providerId;
		final AuthProvider provider;

		OAuthUserInfo(String email, String name, String imageUrl, String providerId, AuthProvider provider) {

			this.email = email;
			this.name = name;
			this.imageUrl = imageUrl;
			this.providerId = providerId;
			this.provider = provider;
		}
	}
}
