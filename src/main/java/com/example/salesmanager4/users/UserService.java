package com.example.salesmanager4.users;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    public final PasswordEncoder passwordEncoder;

    public Iterable<UserDTO> getAllUsers() {
        return userRepository.findAll();
    }

    public void saveUser(UserDTO user) {
        userRepository.save(user);
    }

    public void deactivateUser(String username) {
        userRepository.deactivateUser(username);
    }

    public void activateUser(String username) {
        userRepository.activateUser(username);
    }

    public UserDTO getUserByUsername(String username) {
        return userRepository.findById(username).orElse(null);
    }

    public String resetUserPassword(String userId) {
        UserDTO user = userRepository.findById(userId)
            .orElseThrow();

        // Generate the random plain-text password
        String tempPassword = PasswordGenerator.generateTemporaryPassword(12);

        // Securely hash and save
        UserDTO updatedUser = new UserDTO(user.username(), passwordEncoder.encode(tempPassword), user.enabled(), user.authorities());
        userRepository.save(updatedUser);

        // Return plain-text only once so admin can give it to the user
        return tempPassword;
    }
}
