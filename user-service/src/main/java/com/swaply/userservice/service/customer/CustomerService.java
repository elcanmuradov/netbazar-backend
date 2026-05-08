package com.swaply.userservice.service.customer;

import com.swaply.userservice.client.ProductClient;
import com.swaply.userservice.dto.user.ChangePasswordRequest;
import com.swaply.userservice.dto.user.UserDto;
import com.swaply.userservice.service.MediaStorageService;
import feign.FeignException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.multipart.MultipartFile;
import com.swaply.userservice.entity.User;
import com.swaply.userservice.exception.AuthException;
import com.swaply.userservice.mapper.UserMapper;
import com.swaply.userservice.repository.AdminRepository;
import com.swaply.userservice.repository.SellerRepository;
import com.swaply.userservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class CustomerService {
    private final UserRepository userRepository;
    private final SellerRepository sellerRepository;
    private final AdminRepository adminRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final ProductClient productClient;
    private final MediaStorageService mediaStorageService;


    public void changePassword(ChangePasswordRequest request, Authentication authentication) {
        User user = userRepository.findByEmail(authentication.getName()).orElseThrow(() -> new AuthException("User not found"));
        if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
            throw new AuthException("Old password is incorrect");
        }
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
    }

    public UserDto getUserProfile(Authentication authentication) {
        User user = userRepository.findByEmail(authentication.getName()).orElseThrow(() -> new AuthException(authentication.getName() + " User not found"));
        return userMapper.entityToDto(user);
    }

    public UserDto addProductToFavorites(UUID id, Authentication authentication) {
        User user = userRepository.findByEmail(authentication.getName()).orElseThrow(() -> new AuthException(authentication.getName() + " User not found"));
        List<UUID> favorites = Optional.ofNullable(user.getFavoritedProductsIds()).orElse(new ArrayList<>());
        if (!favorites.contains(id)) {
            favorites.add(id);
            user.setFavoritedProductsIds(favorites);
            userRepository.save(user);
        }
        return userMapper.entityToDto(user);
    }

    public void removeProductFromFavorites(UUID id, Authentication authentication) {
        User user = userRepository.findByEmail(authentication.getName()).orElseThrow(() -> new AuthException(authentication.getName() + " User not found"));
        List<UUID> favorites = user.getFavoritedProductsIds();
        favorites.remove(id);
        user.setFavoritedProductsIds(favorites);
        userRepository.save(user);
    }

    public UserDto getUserById(UUID id) {
        // Try to find in Customers
        var user = userRepository.findById(id);
        if (user.isPresent()) {
            return userMapper.entityToDto(user.get());
        }

        // Try to find in Sellers
        var seller = sellerRepository.findById(id);
        if (seller.isPresent()) {
            return userMapper.sellerToDto(seller.get());
        }

        // Try to find in Admins
        var admin = adminRepository.findById(id);
        if (admin.isPresent()) {
            return userMapper.adminToDto(admin.get());
        }

        throw new AuthException("User not found with id: " + id);
    }

    public List<UUID> getUserFavorites(Authentication authentication) {
        User user = userRepository.findByEmail(authentication.getName()).orElseThrow(() -> new AuthException(authentication.getName() + " User not found"));
        List<UUID> favorites = new ArrayList<>();
        List<UUID> existingFavoriteIds = Optional.ofNullable(user.getFavoritedProductsIds()).orElse(new ArrayList<>());
        List<UUID> validFavoriteIds = new ArrayList<>();

        existingFavoriteIds.forEach(id -> {
            try {
                Boolean active = productClient.isActiveProduct(id).getData();
                if (Boolean.TRUE.equals(active)) {
                    favorites.add(id);
                }
                validFavoriteIds.add(id);
            } catch (FeignException.NotFound ex) {
                log.warn("Favorite product no longer exists, removing stale id: {}", id);
            }
        });

        if (validFavoriteIds.size() != existingFavoriteIds.size()) {
            user.setFavoritedProductsIds(validFavoriteIds);
            userRepository.save(user);
        }

        return favorites;
    }



    public void changeProfilePhoto(MultipartFile file, Authentication authentication) {
        try {
            mediaStorageService.uploadProfilePhotoAsync(
                    file,
                    authentication.getName()
            );
        } catch (Exception e) {
            throw new AuthException("Profil şəkli oxunmadı");
        }
    }
}
