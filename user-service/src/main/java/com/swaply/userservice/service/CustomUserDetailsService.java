package com.swaply.userservice.service;

import com.swaply.userservice.exception.AuthException;
import com.swaply.userservice.repository.AdminRepository;
import com.swaply.userservice.repository.SellerRepository;
import com.swaply.userservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {
    private final UserRepository userRepository;
    private final AdminRepository adminRepository;
    private final SellerRepository sellerRepository;
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Optional<com.swaply.userservice.entity.User> user = userRepository.findByEmail(username);
        if (user.isPresent()) {
            return user.get();
        }
        
        Optional<com.swaply.userservice.entity.Admin> admin = adminRepository.findByEmail(username);
        if (admin.isPresent()) {
            return admin.get();
        }

        Optional<com.swaply.userservice.entity.Seller> seller = sellerRepository.findByEmail(username);
        if (seller.isPresent()) {
            return seller.get();
        } else {
            throw new UsernameNotFoundException("User not found with username: " + username);
        }
    }
}