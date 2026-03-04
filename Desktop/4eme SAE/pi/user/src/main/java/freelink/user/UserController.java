package freelink.user;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    public ResponseEntity<User> getCurrentUser(Authentication authentication) {
        if (authentication == null) {
            return ResponseEntity.status(401).build();
        }
        Object principal = authentication.getPrincipal();
        if (principal instanceof OidcUser oidcUser) {
            return ResponseEntity.ok(userService.syncUser(oidcUser));
        } else if (principal instanceof Jwt jwt) {
            return ResponseEntity.ok(userService.syncUser(jwt));
        }
        return ResponseEntity.status(401).build();
    }

    @GetMapping
    public ResponseEntity<List<User>> getAllUsers(Authentication authentication) {
        if (authentication == null) {
            return ResponseEntity.status(401).build();
        }

        // Get current user and check if they're admin
        User currentUser = null;
        Object principal = authentication.getPrincipal();
        if (principal instanceof OidcUser oidcUser) {
            currentUser = userService.syncUser(oidcUser);
        } else if (principal instanceof Jwt jwt) {
            currentUser = userService.syncUser(jwt);
        }

        if (currentUser == null || !hasRole(currentUser, "admin")) {
            return ResponseEntity.status(403).build();
        }

        return ResponseEntity.ok(userService.findAll());
    }

    @GetMapping("/{id}")
    public User getUserById(@PathVariable UUID id) {
        return userService.findById(id);
    }

    @PutMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public User updateUser(@PathVariable UUID id, @RequestBody User user) {
        return userService.update(id, user);
    }

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody freelink.user.dto.UserDTO userDTO) {
        try {
            userService.registerUser(userDTO);
            return ResponseEntity.status(201).build();
        } catch (RuntimeException e) {
            if (e.getMessage() != null && e.getMessage().contains("already exists")) {
                return ResponseEntity.status(409).body("Email already in use. Please use a different email.");
            }
            return ResponseEntity.status(500).body("Registration failed: " + e.getMessage());
        }
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<Void> forgotPassword(@RequestBody String email) {
        userService.forgotPassword(email);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public void deleteUser(@PathVariable UUID id) {
        userService.delete(id);
    }

    @GetMapping("/test-auth")
    public ResponseEntity<String> testAuth(Authentication authentication) {
        if (authentication == null) {
            return ResponseEntity.ok("No authentication");
        }
        Object principal = authentication.getPrincipal();
        String info = "Auth type: " + authentication.getClass().getName() + "\n";
        info += "Principal type: " + (principal != null ? principal.getClass().getName() : "null") + "\n";

        if (principal instanceof Jwt jwt) {
            info += "JWT Subject: " + jwt.getSubject() + "\n";
            info += "JWT Email: " + jwt.getClaimAsString("email") + "\n";
        } else if (principal instanceof OidcUser oidcUser) {
            info += "OIDC Email: " + oidcUser.getEmail() + "\n";
        }

        return ResponseEntity.ok(info);
    }

    @PostMapping("/become-freelancer")
    public ResponseEntity<User> becomeFreelancer(Authentication authentication,
            @RequestBody freelink.user.dto.FreelancerProfileDTO profileDTO) {

        System.out.println("=== BECOME FREELANCER REQUEST ===");
        System.out
                .println("Authentication: " + (authentication != null ? authentication.getClass().getName() : "null"));
        System.out.println("ProfileDTO: " + profileDTO);

        if (authentication == null) {
            System.out.println("ERROR: No authentication provided");
            return ResponseEntity.status(401).body(null);
        }

        User user = null;
        Object principal = authentication.getPrincipal();
        System.out.println("Principal type: " + (principal != null ? principal.getClass().getName() : "null"));

        if (principal instanceof OidcUser oidcUser) {
            System.out.println("Using OidcUser authentication");
            user = userService.syncUser(oidcUser);
        } else if (principal instanceof Jwt jwt) {
            System.out.println("Using JWT authentication");
            System.out.println("JWT subject: " + jwt.getSubject());
            System.out.println("JWT email: " + jwt.getClaimAsString("email"));
            user = userService.syncUser(jwt);
        } else {
            System.out.println("ERROR: Unknown principal type");
            return ResponseEntity.status(401).body(null);
        }

        if (user == null) {
            System.out.println("ERROR: User not found after sync");
            return ResponseEntity.status(401).body(null);
        }

        System.out.println("Found user: " + user.getEmail() + " (ID: " + user.getId() + ")");

        try {
            System.out.println("Calling becomeFreelancer service method...");
            User updatedUser = userService.becomeFreelancer(user, profileDTO);
            System.out.println("SUCCESS: User updated to freelancer");
            return ResponseEntity.ok(updatedUser);
        } catch (Exception e) {
            System.out.println("ERROR: Exception during profile update");
            e.printStackTrace();
            return ResponseEntity.status(500).body(null);
        }
    }

    @PostMapping("/set-role")
    public ResponseEntity<User> setRole(@RequestParam String email, @RequestParam String role) {
        User user = userService.findByEmail(email);
        if (user == null)
            return ResponseEntity.notFound().build();
        user.setRole(role);
        return ResponseEntity.ok(userService.update(user.getId(), user));
    }

    @PostMapping("/cleanup-duplicates")
    @PreAuthorize("hasRole('admin')")
    public ResponseEntity<String> cleanupDuplicates() {
        List<User> allUsers = userService.findAll();
        Map<String, List<User>> usersByEmail = allUsers.stream()
                .filter(u -> u.getEmail() != null)
                .collect(java.util.stream.Collectors.groupingBy(u -> u.getEmail().toLowerCase().trim()));

        int cleaned = 0;
        for (Map.Entry<String, List<User>> entry : usersByEmail.entrySet()) {
            List<User> duplicates = entry.getValue();
            if (duplicates.size() > 1) {
                System.out.println(">>> CLEANUP: Found " + duplicates.size() + " records for " + entry.getKey());
                // Keep the one that is admin or freelancer, or the oldest one
                User toKeep = duplicates.stream()
                        .sorted((u1, u2) -> {
                            int p1 = getRolePriority(u1);
                            int p2 = getRolePriority(u2);
                            if (p1 != p2)
                                return p1 - p2;
                            return u1.getCreatedAt().compareTo(u2.getCreatedAt());
                        })
                        .findFirst().get();

                for (User u : duplicates) {
                    if (!u.getId().equals(toKeep.getId())) {
                        System.out.println(
                                "    DELETING duplicate record: " + u.getId() + " (Role: " + u.getRole() + ")");
                        userService.delete(u.getId());
                        cleaned++;
                    }
                }
            }
        }
        return ResponseEntity.ok("Cleaned " + cleaned + " duplicate records.");
    }

    private boolean hasRole(User user, String role) {
        if (user == null || user.getRole() == null)
            return false;
        return java.util.Arrays.asList(user.getRole().split(","))
                .stream().map(String::trim)
                .anyMatch(r -> r.equalsIgnoreCase(role));
    }

    private int getRolePriority(User u) {
        if (hasRole(u, "admin"))
            return 0;
        if (hasRole(u, "freelancer"))
            return 1;
        return 2;
    }
}
