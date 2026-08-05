package com.worktime.service;

import com.worktime.dto.auth.ChangePasswordRequest;
import com.worktime.dto.user.CreateUserRequest;
import com.worktime.dto.user.UpdateUserRequest;
import com.worktime.dto.user.UserResponse;
import com.worktime.entity.User;
import com.worktime.exception.DuplicateResourceException;
import com.worktime.exception.ResourceNotFoundException;
import com.worktime.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public UserResponse createUser(CreateUserRequest request){
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("Email is already in use");
        }

        User user = new User();

        user.setFirstName(request.getFirstName());
        user.setLastName((request.getLastName()));
        user.setEmail(request.getEmail());
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
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found!"));

        return toResponse(user);
    }

    public UserResponse patchUser(Long id, UpdateUserRequest request){
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found!"));

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
            if (request.getEmail().isBlank()) {
                throw new IllegalArgumentException("Email cannot be blank");
            }

            if (!request.getEmail().equalsIgnoreCase(user.getEmail())
                    && userRepository.existsByEmail(request.getEmail())) {
                throw new DuplicateResourceException("Email is already in use");
            }

            user.setEmail(request.getEmail());
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

    public void changePassword(String email, ChangePasswordRequest request) {
        User user = userRepository.findByEmail(email)
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