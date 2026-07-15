package com.example.salesmanager4.users;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.JdbcUserDetailsManager;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.salesmanager4.employees.Employee;
import com.example.salesmanager4.employees.EmployeeService;
import com.example.salesmanager4.util.Breadcrumb;

import lombok.RequiredArgsConstructor;


@Controller
@RequiredArgsConstructor
@RequestMapping("/users")
public class UserController {

    private final JdbcUserDetailsManager userDetailsManager;
    private final UserRepository userRepository; // TODO refactor to use UserService
    private final UserService userService;
    private final EmployeeService employeeService;
    private final PasswordEncoder passwordEncoder;

    @GetMapping
    public String getUsersPage(Model model) {
        Iterable<UserDTO> users = userRepository.findAllOrderByUsername();
        model.addAttribute("users", users);
        model.addAttribute("employeeNames", employeeNameMap());
        model.addAttribute("breadcrumbs", List.of(
            new Breadcrumb("Home", "/"),
            new Breadcrumb("Users", null)
        ));
        return "user/list::content";
    }

    @GetMapping("/create")
    public String showCreateUserForm(Model model) {
        model.addAttribute("breadcrumbs", List.of(
            new Breadcrumb("Home", "/"),
            new Breadcrumb("Users", "/users"),
            new Breadcrumb("Create User", null)
        ));
        return "user/create::content";
    }

    @PostMapping("/create")
    public String addUser(@RequestParam String username,
            @RequestParam String password,
            @RequestParam(required = false, defaultValue = "") String[] authorities,
            @RequestParam(required = false) String employeeId,
            Model model,
            RedirectAttributes ra) {

        Long empId = parseId(employeeId);

        if (userDetailsManager.userExists(username)) {
            return createFormWithError(model, username, "A user with that username already exists.");
        }
        if (userService.isEmployeeLinkedToOther(empId, username)) {
            return createFormWithError(model, username, "That employee is already linked to another user.");
        }

        String[] roles = authorities.length > 0 ? authorities : new String[]{"USER"};
        UserDetails user = User.builder()
            .username(username)
            .password(passwordEncoder.encode(password))
            .roles(roles)
            .build();
        userDetailsManager.createUser(user);
        userService.linkEmployee(username, empId);

        ra.addFlashAttribute("toastMessage", "User created.");
        return "redirect:/users";
    }

    @GetMapping("/{username}/link")
    public String showLinkForm(@PathVariable String username, Model model) {
        UserDTO user = userService.getUserByUsername(username);
        model.addAttribute("user", user);
        if (user.employeeId() != null) {
            Employee emp = employeeService.findById(user.employeeId());
            model.addAttribute("currentEmployeeName", emp.getKnownName());
        }
        model.addAttribute("breadcrumbs", List.of(
            new Breadcrumb("Home", "/"),
            new Breadcrumb("Users", "/users"),
            new Breadcrumb("Link Employee", null)
        ));
        return "user/link::content";
    }

    @PostMapping("/link")
    public String linkEmployee(@RequestParam String username,
            @RequestParam(required = false) String employeeId,
            Model model,
            RedirectAttributes ra) {

        Long empId = parseId(employeeId);
        try {
            userService.linkEmployee(username, empId);
            ra.addFlashAttribute("toastMessage", empId == null ? "Employee unlinked." : "Employee linked.");
            return "redirect:/users";
        } catch (IllegalStateException e) {
            UserDTO user = userService.getUserByUsername(username);
            model.addAttribute("user", user);
            if (empId != null) {
                model.addAttribute("currentEmployeeName", employeeService.findById(empId).getKnownName());
            }
            model.addAttribute("error", e.getMessage());
            model.addAttribute("breadcrumbs", List.of(
                new Breadcrumb("Home", "/"),
                new Breadcrumb("Users", "/users"),
                new Breadcrumb("Link Employee", null)
            ));
            return "user/link::content";
        }
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
        model.addAttribute("breadcrumbs", List.of(
            new Breadcrumb("Home", "/"),
            new Breadcrumb("Users", "/users"),
            new Breadcrumb("Reset Password", null)
        ));
        return "user/reset_password::content";
    }

    @PostMapping("/reset-password")
    public String resetPassword(@RequestParam String username, Model model) {
        userService.getUserByUsername(username); // to check if user exists
        String tempPassword = userService.resetUserPassword(username);
        model.addAttribute("tempPassword", tempPassword);
        model.addAttribute("breadcrumbs", List.of(
            new Breadcrumb("Home", "/"),
            new Breadcrumb("Users", "/users"),
            new Breadcrumb("Reset Password", null)
        ));
        return "user/reset_password::content";
    }

    // ------------------------------------------------------------------
    // Helpers
    // ------------------------------------------------------------------

    private Long parseId(String raw) {
        return (raw == null || raw.isBlank()) ? null : Long.valueOf(raw);
    }

    private Map<Long, String> employeeNameMap() {
        Map<Long, String> names = new HashMap<>();
        for (Employee e : employeeService.findAll()) {
            names.put(e.getId(), e.getKnownName());
        }
        return names;
    }

    private String createFormWithError(Model model, String username, String error) {
        model.addAttribute("error", error);
        model.addAttribute("username", username);
        model.addAttribute("breadcrumbs", List.of(
            new Breadcrumb("Home", "/"),
            new Breadcrumb("Users", "/users"),
            new Breadcrumb("Create User", null)
        ));
        return "user/create::content";
    }
}
