package com.example.salesmanager4.users;

import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.JdbcUserDetailsManager;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import lombok.RequiredArgsConstructor;




@Controller
@RequiredArgsConstructor
@RequestMapping("/users")
public class UserController {

    private final JdbcUserDetailsManager userDetailsManager;
    private final UserRepository userRepository; // TODO refactor to use UserService
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;

    // @GetMapping
    // public String getUsersPage(Model model) {
    //     List<String> usernames = jdbcTemplate.queryForList("SELECT username FROM users", String.class);
    //     model.addAttribute("users", usernames);
    //     model.addAttribute("content", "users");
    //     return "index";
    // }

    @GetMapping
    public String getUsersPage(Model model) {
        Iterable<UserDTO> users = userRepository.findAllOrderByUsername();
        model.addAttribute("users", users);
        // model.addAttribute("content", "users");
        // return "index";

        return "user/list::content";
    }
    
    @PostMapping("/create")
    public String addUser(@RequestParam String username, @RequestParam String password, @RequestParam(required = false, defaultValue = "") String[] authorities) {
        String[] roles = authorities.length > 0 ? authorities : new String[]{"USER"};
        UserDetails user = User.builder()
            .username(username)
            .password(passwordEncoder.encode(password))
            .roles(roles)
            .build();
        userDetailsManager.createUser(user);
        return "user/create::content";
    }

    @GetMapping("/create")
    public String showCreateUserForm(Model model) {
        // model.addAttribute("content", "users_create");
        // return "index";
        return "user/create::content";
    }

    @PostMapping("/delete")
    @ResponseBody
    public String deleteUser(@RequestParam String username) {
        userDetailsManager.deleteUser(username);
        return "";
    }

    @PostMapping("/deactivate")
    public String deactivateUser(@RequestParam String username, Model model, RedirectAttributes redirectAttributes) {
        String currentPrincipalName = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication().getName();
        if (currentPrincipalName.equals(username)) {
            redirectAttributes.addFlashAttribute("error", "You cannot deactivate your own account.");
            return "redirect:/users";
        }
        redirectAttributes.addFlashAttribute("toastMessage", "User deactivated successfully.");
        userRepository.deactivateUser(username);
        return "redirect:/users";
    }

    @PostMapping("/activate")
    public String activateUser(@RequestParam String username, RedirectAttributes redirectAttributes) {
        userRepository.activateUser(username);
        redirectAttributes.addFlashAttribute("toastMessage", "User activated successfully.");
        return "redirect:/users";
    }

    @GetMapping("/change-password")
    public String showChangePasswordForm(Model model) {
        
        return "user/change_password::content";
    }

    @PostMapping("/change-password")
    public String changePassword(@RequestParam String currentPassword, @RequestParam String newPassword, @RequestParam String confirmNewPassword, Model model, RedirectAttributes redirectAttributes) {
        String currentPrincipalName = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication().getName();
        UserDetails userDetails = userDetailsManager.loadUserByUsername(currentPrincipalName);
        if (!passwordEncoder.matches(currentPassword, userDetails.getPassword())) {
            model.addAttribute("errorMessage", "Current password is incorrect.");
            return "user/change_password::content";
        }
        
        if (!newPassword.equals(confirmNewPassword)) {
            model.addAttribute("errorMessage", "New passwords do not match.");
            return "user/change_password::content";
        }
        try {
            userDetailsManager.changePassword(currentPassword, passwordEncoder.encode(newPassword));
            model.addAttribute("successMessage", "Password changed successfully.");
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Failed to change password. Please ensure the old password is correct.");
        }
        return "user/change_password::content";
    }

    @GetMapping("/reset-password")
    public String showResetPasswordForm(Model model) {
        return "user/reset_password::content";
    }

    @PostMapping("/reset-password")
    public String resetPassword(@RequestParam String username, Model model) {
        userService.getUserByUsername(username); // to check if user exists
        String tempPassword = userService.resetUserPassword(username);
        model.addAttribute("tempPassword", tempPassword);
        return "user/reset_password::content";
    }

}

