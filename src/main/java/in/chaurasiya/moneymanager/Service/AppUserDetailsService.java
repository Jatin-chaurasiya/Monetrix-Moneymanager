package in.chaurasiya.moneymanager.Service;

import in.chaurasiya.moneymanager.Entity.ProfileEntity;
import in.chaurasiya.moneymanager.Repository.ProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AppUserDetailsService implements UserDetailsService {

    private final ProfileRepository profileRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {

        ProfileEntity existingProfile = profileRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Profile not found with email: " + email));

        if (existingProfile.getIsActive() == null || !existingProfile.getIsActive()) {
            throw new UsernameNotFoundException("Account is not activated for email: " + email);
        }

        return User.builder()
                .username(existingProfile.getEmail())
                .password(existingProfile.getPassword())
                .roles("USER")
                .build();
    }
}