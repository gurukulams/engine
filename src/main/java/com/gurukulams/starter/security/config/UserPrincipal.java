package com.gurukulams.starter.security.config;

import com.gurukulams.core.model.LearnerProfile;
import com.gurukulams.core.payload.Learner;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * The type User principal.
 */
public class UserPrincipal implements OAuth2User, UserDetails {

    /**
     * declares variable name.
     */
    private String name;
    /**
     * declares variable password.
     */
    private String password;

    /**
     * declares variable displayName.
     */
    private String displayName;

    /**
     * declares variable profilePicture.
     */
    private String profilePicture;

    /**
     * declares variable isRegistered.
     */
    private boolean isRegistered;

    /**
     * declares collection of authority.
     */
    private Collection<? extends GrantedAuthority> authorities;
    /**
     * declares map of getAttributes().
     */
    private Map<String, Object> attributes;

    /**
     * Instantiates a new User principal.
     *
     * @param theName        the name
     * @param theDisplayName the Display Name
     * @param thePassword    the password
     * @param theProfilePicture
     * @param registered
     * @param theAuthorities the authorities
     */
    public UserPrincipal(final String theName,
                         final String theDisplayName,
                         final String thePassword,
                         final String theProfilePicture,
                         final boolean registered,
                         final Collection<? extends GrantedAuthority>
                                 theAuthorities) {

        this.name = theName;
        this.displayName = theDisplayName;
        this.password = thePassword;
        this.profilePicture = theProfilePicture;
        this.isRegistered = registered;
        this.authorities = theAuthorities;
    }

    /**
     * Create user principal.
     *
     * @param user the user
     * @param profile
     * @return the user principal
     */
    public static UserPrincipal create(final Learner user,
                                       final Optional<LearnerProfile> profile) {
        final List<GrantedAuthority> authorities = Collections.
                singletonList(new SimpleGrantedAuthority("ROLE_USER"));

        return profile.map(learnerProfile -> new UserPrincipal(
                user.userHandle(),
                learnerProfile.name(),
                user.password(),
                user.imageUrl(),
                true,
                authorities
        )).orElseGet(() -> new UserPrincipal(
                user.userHandle(),
                null,
                user.password(),
                user.imageUrl(),
                false,
                authorities
        ));
    }

    /**
     * Create user principal.
     *
     * @param user       the user
     * @param profile
     * @param attributes the attributes
     * @return the user principal
     */
    public static UserPrincipal create(final Learner user,
                                       final Optional<LearnerProfile> profile,
                                       final Map<String, Object> attributes) {
        final UserPrincipal userPrincipal = UserPrincipal.create(user, profile);
        userPrincipal.setAttributes(attributes);
        return userPrincipal;
    }
    /**
     * gets the profilePicture.
     *
     * @return profilePicture
     */
    public String getProfilePicture() {
        return profilePicture;
    }
    /**
     * gets the isRegistered.
     *
     * @return isRegistered
     */
    public boolean isRegistered() {
        return isRegistered;
    }

    /**
     * gets the password.
     *
     * @return password
     */
    @Override
    public String getPassword() {
        return password;
    }

    /**
     * gets the username.
     *
     * @return username
     */
    @Override
    public String getUsername() {
        return name;
    }

    /**
     * gets the value of accountnotexpired.
     *
     * @return true
     */
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    /**
     * returns value of isaccountnotlocked.
     *
     * @return true
     */
    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    /**
     * returns value ofiscredentialsnonexpired.
     *
     * @return true
     */
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    /**
     * returns value of isEnabled.
     *
     * @return true
     */
    @Override
    public boolean isEnabled() {
        return true;
    }

    /**
     * gets the values of authorities.
     *
     * @return authorities
     */
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }



    /**
     * Sets getAttributes().
     *
     * @param theAttributes the attributes
     */
    public void setAttributes(final Map<String, Object> theAttributes) {
        this.attributes = theAttributes;
    }

    /**
     * gets the getAttributes().
     *
     * @return map of attributes
     */
    @Override
    public Map<String, Object> getAttributes() {
        return attributes;
    }
    /**
     * gets the name.
     *
     * @return name
     */
    @Override
    public String getName() {
        return name;
    }

    /**
     * gets the value for display Name.
     *
     * @return displayName
     */
    public String getDisplayName() {
        return displayName;
    }

}
