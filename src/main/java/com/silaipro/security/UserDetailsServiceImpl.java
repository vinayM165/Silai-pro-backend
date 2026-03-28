package com.silaipro.security;

import com.silaipro.entity.User;
import com.silaipro.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String login) throws UsernameNotFoundException {
        User user = userRepository.findByPhoneOrEmail(login)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with phone/email: " + login));

        return new CustomUserDetails(user);
    }
}
