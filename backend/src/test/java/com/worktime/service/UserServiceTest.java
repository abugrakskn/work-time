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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    @Test
    void createUserShouldCreateActiveUserWithNormalizedEmail() {
        // Arrange
        CreateUserRequest request =
                org.mockito.Mockito.mock(
                        CreateUserRequest.class
                );

        when(request.getFirstName())
                .thenReturn("Ahmet");

        when(request.getLastName())
                .thenReturn("Keskin");

        when(request.getEmail())
                .thenReturn("  Ahmet@Example.COM ");

        when(request.getPassword())
                .thenReturn("Password123!");

        when(request.getRole())
                .thenReturn(UserRole.EMPLOYEE);

        when(
                userRepository.existsByEmailIgnoreCase(
                        "ahmet@example.com"
                )
        ).thenReturn(false);

        when(
                passwordEncoder.encode("Password123!")
        ).thenReturn("encoded-password");

        when(userRepository.save(any(User.class)))
                .thenAnswer(invocation ->
                        invocation.getArgument(0)
                );

        // Act
        UserResponse response =
                userService.createUser(request);

        // Assert
        assertThat(response).isNotNull();

        ArgumentCaptor<User> userCaptor =
                ArgumentCaptor.forClass(User.class);

        verify(userRepository).save(
                userCaptor.capture()
        );

        User savedUser = userCaptor.getValue();

        assertThat(savedUser.getFirstName())
                .isEqualTo("Ahmet");

        assertThat(savedUser.getLastName())
                .isEqualTo("Keskin");

        assertThat(savedUser.getEmail())
                .isEqualTo("ahmet@example.com");

        assertThat(savedUser.getPassword())
                .isEqualTo("encoded-password");

        assertThat(savedUser.getRole())
                .isEqualTo(UserRole.EMPLOYEE);

        assertThat(savedUser.isActive()).isTrue();

        verify(passwordEncoder)
                .encode("Password123!");

        verify(userRepository)
                .existsByEmailIgnoreCase(
                        "ahmet@example.com"
                );
    }

    @Test
    void createUserShouldThrowWhenEmailAlreadyExists() {
        // Arrange
        CreateUserRequest request =
                org.mockito.Mockito.mock(
                        CreateUserRequest.class
                );

        when(request.getEmail())
                .thenReturn("Ahmet@Example.com");

        when(
                userRepository.existsByEmailIgnoreCase(
                        "ahmet@example.com"
                )
        ).thenReturn(true);

        // Act & Assert
        assertThatThrownBy(() ->
                userService.createUser(request)
        )
                .isInstanceOf(
                        DuplicateResourceException.class
                )
                .hasMessage("Email is already in use");

        verify(userRepository, never())
                .save(any(User.class));

        verify(passwordEncoder, never())
                .encode(anyString());
    }

    @Test
    void getUserByIdShouldThrowWhenUserDoesNotExist() {
        // Arrange
        when(userRepository.findById(99L))
                .thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() ->
                userService.getUserById(99L)
        )
                .isInstanceOf(
                        ResourceNotFoundException.class
                )
                .hasMessage("User not found!");
    }

    @Test
    void changePasswordShouldEncodeAndSaveNewPassword() {
        // Arrange
        User user = new User();
        user.setEmail("ahmet@example.com");
        user.setPassword("old-encoded-password");

        ChangePasswordRequest request =
                org.mockito.Mockito.mock(
                        ChangePasswordRequest.class
                );

        when(request.getCurrentPassword())
                .thenReturn("OldPassword123!");

        when(request.getNewPassword())
                .thenReturn("NewPassword123!");

        when(
                userRepository.findByEmailIgnoreCase(
                        "ahmet@example.com"
                )
        ).thenReturn(Optional.of(user));

        when(
                passwordEncoder.matches(
                        "OldPassword123!",
                        "old-encoded-password"
                )
        ).thenReturn(true);

        when(
                passwordEncoder.encode(
                        "NewPassword123!"
                )
        ).thenReturn("new-encoded-password");

        // Act
        userService.changePassword(
                "ahmet@example.com",
                request
        );

        // Assert
        assertThat(user.getPassword())
                .isEqualTo("new-encoded-password");

        verify(userRepository).save(user);
    }

    @Test
    void changePasswordShouldThrowWhenCurrentPasswordIsIncorrect() {
        // Arrange
        User user = new User();
        user.setPassword("old-encoded-password");

        ChangePasswordRequest request =
                org.mockito.Mockito.mock(
                        ChangePasswordRequest.class
                );

        when(request.getCurrentPassword())
                .thenReturn("WrongPassword");

        when(
                userRepository.findByEmailIgnoreCase(
                        "ahmet@example.com"
                )
        ).thenReturn(Optional.of(user));

        when(
                passwordEncoder.matches(
                        "WrongPassword",
                        "old-encoded-password"
                )
        ).thenReturn(false);

        // Act & Assert
        assertThatThrownBy(() ->
                userService.changePassword(
                        "ahmet@example.com",
                        request
                )
        )
                .isInstanceOf(
                        IllegalArgumentException.class
                )
                .hasMessage(
                        "Current password is incorrect!"
                );

        verify(userRepository, never()).save(any(User.class));
        verify(passwordEncoder, never()).encode(anyString());
    }

    @Test
    void patchUserShouldPreventAdminFromDeactivatingOwnAccount() {
        // Arrange
        User admin = new User();
        admin.setEmail("admin@example.com");
        admin.setRole(UserRole.ADMIN);
        admin.setActive(true);

        UpdateUserRequest request =
                org.mockito.Mockito.mock(
                        UpdateUserRequest.class
                );

        when(request.getActive()).thenReturn(false);
        when(request.getRole()).thenReturn(null);

        when(userRepository.findById(1L))
                .thenReturn(Optional.of(admin));

        // Act & Assert
        assertThatThrownBy(() ->
                userService.patchUser(
                        1L,
                        request,
                        "ADMIN@example.com"
                )
        )
                .isInstanceOf(
                        IllegalArgumentException.class
                )
                .hasMessage(
                        "You cannot deactivate your own account"
                );

        verify(userRepository, never()).save(any(User.class));
    }
}