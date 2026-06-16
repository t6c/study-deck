package org.fpt.studydeck.security;

import org.fpt.studydeck.domain.auth.AppUser;
import org.fpt.studydeck.repository.auth.AppUserRepository;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class AppUserDetailsService implements UserDetailsService {

    private final AppUserRepository appUserRepository;

    public AppUserDetailsService(AppUserRepository appUserRepository) {
        this.appUserRepository = appUserRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) {
        AppUser user = appUserRepository.findByEmail(AppUser.normalizeEmail(username))
            .orElseThrow(() -> new UsernameNotFoundException("User not found."));

        return User.withUsername(user.getEmail())
            .password(user.getPasswordHash())
            .authorities("ROLE_" + user.getRole())
            .build();
    }
}
