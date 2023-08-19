package com.techatpark.workout.starter.security.oauth2;


import com.techatpark.workout.starter.security.config.UserPrincipal;
import com.techatpark.workout.starter.security.exception.OAuth2AuthenticationProcessingException;
import com.techatpark.workout.starter.security.oauth2.user.OAuth2UserInfo;
import com.techatpark.workout.starter.security.oauth2.user.OAuth2UserInfoFactory;
import com.techatpark.workout.starter.security.payload.AuthProvider;
import com.techatpark.workout.starter.security.payload.Learner;
import com.techatpark.workout.starter.security.payload.SignupRequest;
import com.techatpark.workout.starter.security.service.LearnerProfileService;
import com.techatpark.workout.starter.security.service.LearnerService;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.util.StringUtils;

import java.util.Optional;

/**
 * The type Custom o auth 2 user service.
 */
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    /**
     * Learner Details Service.
     */
    private final LearnerService learnerService;

    /**
     * Learner Details Service.
     */
    private final LearnerProfileService learnerProfileService;

    /**
     * CustomOAuth2UserService.
     * @param alearnerProfileService
     * @param anUserRepository user repository
     */
    public CustomOAuth2UserService(final LearnerService
                                           anUserRepository,
                                   final LearnerProfileService
                                           alearnerProfileService) {
        this.learnerService = anUserRepository;
        this.learnerProfileService = alearnerProfileService;
    }

    /**
     * Loads the user.
     *
     * @param oAuth2UserRequest request
     * @return OAuth2User
     * @throws OAuth2AuthenticationException auth to authentication exception
     */
    @Override
    public OAuth2User loadUser(final OAuth2UserRequest oAuth2UserRequest)
            throws OAuth2AuthenticationException {
        final OAuth2User oAuth2User = super.loadUser(oAuth2UserRequest);

        try {
            return processOAuth2User(oAuth2UserRequest, oAuth2User);
        } catch (final AuthenticationException ex) {
            throw ex;
        } catch (final Exception ex) {
            // Throwing an instance of AuthenticationException will
            // trigger the OAuth2AuthenticationFailureHandler
            throw new InternalAuthenticationServiceException(ex.getMessage(),
                    ex.getCause());
        }
    }

    private OAuth2User processOAuth2User(final
                                         OAuth2UserRequest oAuth2UserRequest,
                                         final OAuth2User oAuth2User) {
        final OAuth2UserInfo oAuth2UserInfo = OAuth2UserInfoFactory
                .getOAuth2UserInfo(oAuth2UserRequest.getClientRegistration()
                        .getRegistrationId(), oAuth2User.getAttributes());
        if (!StringUtils.hasLength(oAuth2UserInfo.getEmail())) {
            throw new OAuth2AuthenticationProcessingException(
                    "Email not found from OAuth2 provider");
        }

        final Optional<Learner> userOptional =
                learnerService.readByEmail(oAuth2UserInfo.getEmail());
        Learner user;
        if (userOptional.isPresent()) {
            user = userOptional.get();
            if (!user.provider().equals(AuthProvider.valueOf(
                    oAuth2UserRequest.getClientRegistration()
                            .getRegistrationId()))) {
                throw new OAuth2AuthenticationProcessingException(
                        "Looks like you're signed up with "
                                + user.provider()
                                + " account. Please use your "
                                + user.provider()
                                + " account to login.");
            }
            user = updateExistingUser(user, oAuth2UserInfo);
        } else {
            user = registerNewUser(oAuth2UserRequest, oAuth2UserInfo);
        }

        return UserPrincipal.create(user,
                learnerProfileService.read(user.userHandle()),
                oAuth2User.getAttributes());
    }

    private Learner registerNewUser(final OAuth2UserRequest oAuth2UserRequest,
                                    final OAuth2UserInfo oAuth2UserInfo) {
        final Learner user = new Learner(oAuth2UserInfo.getEmail()
                .substring(0, oAuth2UserInfo.getEmail().indexOf('@')),
                oAuth2UserInfo.getEmail(),
                null,
                oAuth2UserInfo.getImageUrl(), AuthProvider.valueOf(
                oAuth2UserRequest.getClientRegistration()
                        .getRegistrationId()), null, null);
        SignupRequest signupRequest = new SignupRequest();
        signupRequest.setEmail(oAuth2UserInfo.getEmail());
        signupRequest.setPassword(oAuth2UserInfo.getEmail());
        signupRequest.setImageUrl(oAuth2UserInfo.getImageUrl());
        learnerService.signUp(signupRequest, s -> {
            return s;
        });
        return learnerService.readByEmail(oAuth2UserInfo.getEmail()).get();
    }

    private Learner updateExistingUser(final Learner existingUser,
                                       final OAuth2UserInfo oAuth2UserInfo) {
        return learnerService.update(existingUser.userHandle(),
                new Learner(null, oAuth2UserInfo.getEmail(),
                        null,
                        oAuth2UserInfo.getImageUrl(),
                        existingUser.provider(),
                        null, null));
    }

}
