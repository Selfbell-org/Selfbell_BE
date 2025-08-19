package com.selfbell.global.security;

import com.selfbell.user.domain.User;
import com.selfbell.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String phoneNumber) throws UsernameNotFoundException {

        User user = userRepository.findByPhoneNumber(phoneNumber)
                .orElseThrow(() -> new UsernameNotFoundException("존재하지 않는 유저입니다: " + phoneNumber));

        return new org.springframework.security.core.userdetails.User(
                user.getPhoneNumber(),
                user.getPassword(),
                AuthorityUtils.createAuthorityList("ROLE_" + user.getRole().name())
        );
    }

    public UserDetails loadUserById(Long userId) throws UsernameNotFoundException {
        var user = userRepository.findById(userId)
                .orElseThrow(() -> new UsernameNotFoundException("존재하지 않는 유저입니다: " + userId));

        // username = String.valueOf(userId)
        return new org.springframework.security.core.userdetails.User(
                String.valueOf(user.getId()),
                user.getPassword() != null ? user.getPassword() : "",
                AuthorityUtils.createAuthorityList("ROLE_" + user.getRole().name())
        );
    }
}
