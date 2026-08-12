package com.worktime.service;

import com.worktime.dto.auth.ChangePasswordRequest;
import com.worktime.dto.user.CreateUserRequest;
import com.worktime.dto.user.UpdateUserRequest;
import com.worktime.dto.user.UserResponse;
import com.worktime.entity.User;
import com.worktime.entity.UserRole;
import com.worktime.exception.DuplicateResourceException;
import com.worktime.exception.ResourceNotFoundException;
import com.worktime.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Locale;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public UserResponse createUser(CreateUserRequest request){
        String normalizedEmail = normalizedEmail(request.getEmail());

        if (userRepository.existsByEmailIgnoreCase(normalizedEmail)) {
            throw new DuplicateResourceException("Email is already in use");
        }

        User user = new User();

        user.setFirstName(request.getFirstName());
        user.setLastName((request.getLastName()));
        user.setEmail(normalizedEmail);
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(request.getRole());
        user.setActive(true);

        User createdUser = userRepository.save(user);
        return toResponse(createdUser);
    }

    public List<UserResponse> getAllUsers() {

        return userRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public UserResponse getUserById(Long id){
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found!"));

        return toResponse(user);
    }

    public UserResponse getUserByEmail(String email){
        User user = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found!"));

        return toResponse(user);
    }

    public UserResponse patchUser(
            Long id,
            UpdateUserRequest request,
            String currentAdminEmail
    ) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found!"));

        validateAdministratorUpdate(
                user,
                request,
                currentAdminEmail
        );

        if (request.getFirstName() != null){
            if (request.getFirstName().isBlank()){
                throw new IllegalArgumentException("First name cannot be blank!");
            }
            user.setFirstName(request.getFirstName());
        }

        if (request.getLastName() != null){
            if (request.getLastName().isBlank()){
                throw new IllegalArgumentException("Last name cannot be blank!");
            }
            user.setLastName(request.getLastName());
        }

        if (request.getEmail() != null) {
            String normalizedEmail = normalizedEmail(request.getEmail());

            if (normalizedEmail.isBlank()) {
                throw new IllegalArgumentException("Email cannot be blank");
            }

            if (!normalizedEmail.equalsIgnoreCase(user.getEmail())
                    && userRepository.existsByEmailIgnoreCase(normalizedEmail)) {
                throw new DuplicateResourceException("Email is already in use");
            }

            user.setEmail(normalizedEmail);
        }

        if (request.getRole() != null) {
            user.setRole(request.getRole());
        }

        if (request.getActive() != null) {
            user.setActive(request.getActive());
        }

        User updatedUser = userRepository.save(user);
        return toResponse(updatedUser);
    }

    private String normalizedEmail(String email){
        return email.trim().toLowerCase(Locale.ROOT);
    }

    private void validateAdministratorUpdate(
            User user,
            UpdateUserRequest request,
            String currentAdminEmail
    ) {
        boolean updatingOwnAccount =
                user.getEmail().equalsIgnoreCase(currentAdminEmail);

        boolean deactivatingUser =
                Boolean.FALSE.equals(request.getActive());

        boolean removingAdminRole =
                request.getRole() != null
                        && request.getRole() != UserRole.ADMIN;

        if (updatingOwnAccount && deactivatingUser) {
            throw new IllegalArgumentException("You cannot deactivate your own account");
        }

        if (updatingOwnAccount && removingAdminRole) {
            throw new IllegalArgumentException("You cannot remove your own administrator role");
        }

        boolean removingActiveAdmin =
                user.isActive()
                        && user.getRole() == UserRole.ADMIN
                        && (deactivatingUser || removingAdminRole);

        if (!removingActiveAdmin) {
            return;
        }

        long activeAdminCount =
                userRepository.countByRoleAndActiveTrue(UserRole.ADMIN);

        if (activeAdminCount <= 1) {
            throw new IllegalArgumentException("At least one active administrator must remain");
        }
    }

    public void changePassword(String email, ChangePasswordRequest request) {
        User user = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found!"));

        if (!passwordEncoder.matches(
                request.getCurrentPassword(),
                user.getPassword()
        )) {
            throw new IllegalArgumentException("Current password is incorrect!");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));

        userRepository.save(user);
    }

    private UserResponse toResponse(User user){
        return new UserResponse(
                user.getId(),
                user.getFirstName(),
                user.getLastName(),
                user.getEmail(),
                user.getRole(),
                user.isActive()
        );
    }
}