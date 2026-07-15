package com.example.salesmanager4.users;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

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

    /** True if the employee is already linked to a different user. */
    public boolean isEmployeeLinkedToOther(Long employeeId, String username) {
        if (employeeId == null) {
            return false;
        }
        return userRepository.findByEmployeeId(employeeId)
            .filter(existing -> !existing.username().equals(username))
            .isPresent();
    }

    /** Link (or, with a null employeeId, unlink) an employee to a user. */
    public void linkEmployee(String username, Long employeeId) {
        if (isEmployeeLinkedToOther(employeeId, username)) {
            UserDTO existing = userRepository.findByEmployeeId(employeeId).orElseThrow();
            throw new IllegalStateException(
                "That employee is already linked to user '" + existing.username() + "'.");
        }
        userRepository.updateEmployeeId(username, employeeId);
    }

    public String resetUserPassword(String userId) {
        UserDTO user = userRepository.findById(userId)
            .orElseThrow();

        // Generate the random plain-text password
        String tempPassword = PasswordGenerator.generateTemporaryPassword(12);

        // Securely hash and save
        UserDTO updatedUser = user.withPassword(passwordEncoder.encode(tempPassword));
        userRepository.save(updatedUser);

        // Return plain-text only once so admin can give it to the user
        return tempPassword;
    }
}
