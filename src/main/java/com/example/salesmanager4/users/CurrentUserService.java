package com.example.salesmanager4.users;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class CurrentUserService {
    
    private final UserRepository userRepository;

    public CurrentUserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public String getUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            return authentication.getName(); // Returns the username of the authenticated user
        }
        return null; // Return null or handle unauthenticated case as needed
    }

    public Long getEmployeeId() {
        UserDTO user = userRepository.findById(getUsername())
            .orElseThrow(() -> new RuntimeException("Employee ID not found for user"));

        return user.employeeId();
    }

    public boolean hasAuthority(String authority) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        log.info("Authorities: {}", authentication.getAuthorities());

        if (authentication != null && authentication.isAuthenticated()) {
            return authentication.getAuthorities().stream()
                .anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals(authority));
        }
        return false; // Return false or handle unauthenticated case as needed
    }
}
