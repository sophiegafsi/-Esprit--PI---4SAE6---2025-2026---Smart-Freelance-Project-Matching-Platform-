package freelink.user;

import freelink.user.service.KeycloakService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;

import java.time.LocalDateTime;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final NotificationRepository notificationRepository;
    private final KeycloakService keycloakService;
    private final EntityManager entityManager;

    @Override
    public User syncUser(OidcUser oidcUser) {
        String keycloakId = oidcUser.getSubject();
        String email = oidcUser.getEmail() != null ? oidcUser.getEmail().toLowerCase().trim() : null;
        System.out.println(">>> SYNC START (OIDC) - Email: " + email + ", Subject: " + keycloakId);

        // Prioritize finding by EMAIL to catch manually promoted admins
        User user = (email != null ? userRepository.findByEmail(email) : java.util.Optional.<User>empty())
                .map(found -> {
                    System.out.println(
                            "[DEBUG] Found user by email. ID: " + found.getId() + ", Role: " + found.getRole());
                    if (!keycloakId.equals(found.getKeycloakId())) {
                        System.out.println("[DEBUG] Updating matching Keycloak ID.");
                        found.setKeycloakId(keycloakId);
                        return userRepository.save(found);
                    }
                    return found;
                })
                .orElseGet(() -> userRepository.findByKeycloakId(keycloakId)
                        .map(found -> {
                            System.out.println("[DEBUG] Found user by KeycloakId. ID: " + found.getId() + ", Role: "
                                    + found.getRole());
                            return found;
                        })
                        .orElseGet(() -> {
                            System.out.println("[DEBUG] User not found. Creating NEW.");
                            return createNewUser(oidcUser);
                        }));

        if (user.getId() != null) {
            System.out.println("    Forcing DB refresh for user ID: " + user.getId());
            entityManager.flush();
            entityManager.refresh(user);
        }

        return updateExistingUser(user, oidcUser);
    }

    @Override
    public User syncUser(Jwt jwt) {
        String keycloakId = jwt.getSubject();
        String email = jwt.getClaimAsString("email") != null ? jwt.getClaimAsString("email").toLowerCase().trim()
                : null;
        System.out.println("[DEBUG] SYNC START (JWT) - Email: " + email + ", Subject: " + keycloakId);

        // Prioritize finding by EMAIL to catch manually promoted admins
        User user = (email != null ? userRepository.findByEmail(email) : java.util.Optional.<User>empty())
                .map(found -> {
                    System.out.println(
                            "[DEBUG] Found user by email. ID: " + found.getId() + ", Role: " + found.getRole());
                    if (!keycloakId.equals(found.getKeycloakId())) {
                        System.out.println("[DEBUG] Updating matching Keycloak ID.");
                        found.setKeycloakId(keycloakId);
                        return userRepository.save(found);
                    }
                    return found;
                })
                .orElseGet(() -> userRepository.findByKeycloakId(keycloakId)
                        .map(found -> {
                            System.out.println("[DEBUG] Found user by KeycloakId. ID: " + found.getId() + ", Role: "
                                    + found.getRole());
                            return found;
                        })
                        .orElseGet(() -> {
                            System.out.println("[DEBUG] User not found. Creating NEW.");
                            return createNewUser(jwt);
                        }));

        if (user.getId() != null) {
            System.out.println("[DEBUG] Forcing DB refresh for user ID: " + user.getId());
            entityManager.flush();
            entityManager.refresh(user);
        }

        return updateExistingUser(user, jwt);
    }

    private User createNewUser(OidcUser oidcUser) {
        User newUser = User.builder()
                .id(UUID.randomUUID())
                .keycloakId(oidcUser.getSubject())
                .email(oidcUser.getEmail())
                .firstName(oidcUser.getGivenName())
                .lastName(oidcUser.getFamilyName())
                .role(extractRole(oidcUser))
                // .birthDate(extractBirthDate(oidcUser)) // Skip for now or adapt
                // .country(extractCountry(oidcUser))
                .createdAt(LocalDateTime.now())
                .build();
        return userRepository.save(newUser);
    }

    private User createNewUser(Jwt jwt) {
        User newUser = User.builder()
                .id(UUID.randomUUID())
                .keycloakId(jwt.getSubject())
                .email(jwt.getClaimAsString("email"))
                .firstName(jwt.getClaimAsString("given_name"))
                .lastName(jwt.getClaimAsString("family_name"))
                .role(extractRole(jwt))
                .createdAt(LocalDateTime.now())
                .build();
        return userRepository.save(newUser);
    }

    private User updateExistingUser(User existingUser, OidcUser oidcUser) {
        String dbRole = existingUser.getRole() != null ? existingUser.getRole().trim().toLowerCase() : "";
        System.out.println("[DEBUG] UPDATE check (OIDC) - User: " + existingUser.getEmail());
        System.out.println("[DEBUG] DB Role: [" + dbRole + "]");

        if (existingUser.getFirstName() == null || existingUser.getFirstName().isEmpty()) {
            existingUser.setFirstName(oidcUser.getGivenName());
        }
        if (existingUser.getLastName() == null || existingUser.getLastName().isEmpty()) {
            existingUser.setLastName(oidcUser.getFamilyName());
        }

        String tokenRoles = extractRole(oidcUser);
        System.out.println("[DEBUG] Token Roles: [" + tokenRoles + "]");

        // ADDITIVE MERGING:
        Set<String> roles = new java.util.HashSet<>(java.util.Arrays.asList(dbRole.split(",")));
        roles.addAll(java.util.Arrays.asList(tokenRoles.split(",")));
        roles.remove(""); // Remove empty strings from split

        // Ensure "client" is the base if nothing else exists
        if (roles.isEmpty())
            roles.add("client");

        String mergedRoles = String.join(",", roles);
        if (!mergedRoles.equals(dbRole)) {
            System.out.println("[DEBUG] Merged Roles: " + dbRole + " -> " + mergedRoles);
            existingUser.setRole(mergedRoles);
        }

        return userRepository.save(existingUser);
    }

    private User updateExistingUser(User existingUser, Jwt jwt) {
        String dbRole = existingUser.getRole() != null ? existingUser.getRole().trim().toLowerCase() : "";
        System.out.println("[DEBUG] UPDATE check (JWT) - User: " + existingUser.getEmail());
        System.out.println("[DEBUG] DB Role: [" + dbRole + "]");

        if (existingUser.getFirstName() == null || existingUser.getFirstName().isEmpty()) {
            existingUser.setFirstName(jwt.getClaimAsString("given_name"));
        }
        if (existingUser.getLastName() == null || existingUser.getLastName().isEmpty()) {
            existingUser.setLastName(jwt.getClaimAsString("family_name"));
        }

        String tokenRoles = extractRole(jwt);
        System.out.println("[DEBUG] Token Roles: [" + tokenRoles + "]");

        // ADDITIVE MERGING:
        Set<String> roles = new java.util.HashSet<>(java.util.Arrays.asList(dbRole.split(",")));
        roles.addAll(java.util.Arrays.asList(tokenRoles.split(",")));
        roles.remove("");

        if (roles.isEmpty())
            roles.add("client");

        String mergedRoles = String.join(",", roles);
        if (!mergedRoles.equals(dbRole)) {
            System.out.println("[DEBUG] Merged Roles: " + dbRole + " -> " + mergedRoles);
            existingUser.setRole(mergedRoles);
        }

        return userRepository.save(existingUser);
    }

    // ... extractCountry/BirthDate moved or kept for OidcUser ...

    private String extractRole(OidcUser oidcUser) {
        Map<String, Object> realmAccess = oidcUser.getClaim("realm_access");
        return extractRoleFromRealmAccess(realmAccess);
    }

    private String extractRole(Jwt jwt) {
        Map<String, Object> realmAccess = jwt.getClaim("realm_access");
        return extractRoleFromRealmAccess(realmAccess);
    }

    private String extractRoleFromRealmAccess(Map<String, Object> realmAccess) {
        if (realmAccess != null && realmAccess.containsKey("roles")) {
            List<String> roles = (List<String>) realmAccess.get("roles");
            Set<String> filteredRoles = roles.stream()
                    .map(String::toLowerCase)
                    .filter(r -> r.equals("admin") || r.equals("freelancer") || r.equals("client"))
                    .collect(java.util.stream.Collectors.toSet());

            if (filteredRoles.isEmpty())
                return "client";
            return String.join(",", filteredRoles);
        }
        return "client";
    }

    @Override
    public List<User> findAll() {
        return userRepository.findAll();
    }

    @Override
    public User findByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElse(null);
    }

    @Override
    public java.util.Optional<User> findByKeycloakId(String keycloakId) {
        return userRepository.findByKeycloakId(keycloakId);
    }

    @Override
    public User findById(UUID id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    @Override
    public User update(UUID id, User userDetails) {
        User user = findById(id);

        // Basic Info
        user.setFirstName(userDetails.getFirstName());
        user.setLastName(userDetails.getLastName());
        user.setImageUrl(userDetails.getImageUrl());
        user.setBirthDate(userDetails.getBirthDate());
        user.setCountry(userDetails.getCountry());

        // Freelancer Info
        user.setJobTitle(userDetails.getJobTitle());
        user.setBio(userDetails.getBio());
        user.setSkills(userDetails.getSkills());
        user.setHourlyRate(userDetails.getHourlyRate());
        user.setPortfolioUrl(userDetails.getPortfolioUrl());

        // CRITICAL: Actually save the role if it was changed (e.g. by set-role)
        if (userDetails.getRole() != null) {
            user.setRole(userDetails.getRole());
        }

        // Sync with Keycloak
        if (user.getKeycloakId() != null) {
            keycloakService.updateUser(user.getKeycloakId(), user.getFirstName(), user.getLastName());
            if (userDetails.getRole() != null) {
                keycloakService.updateUserRole(user.getKeycloakId(), user.getRole());
            }
        }

        return userRepository.save(user);
    }

    @Override
    public void delete(UUID id) {
        User user = findById(id);
        if (user.getKeycloakId() != null) {
            keycloakService.deleteUser(user.getKeycloakId());
        }
        userRepository.deleteById(id);
    }

    @Override
    public void registerUser(freelink.user.dto.UserDTO userDTO) {
        // 1. Create user in Keycloak and get the Keycloak UUID back
        String keycloakId = keycloakService.createUser(userDTO);

        // 2. Also persist to local H2 DB so the user is immediately available
        try {
            userRepository.findByEmail(userDTO.getEmail().toLowerCase().trim()).ifPresentOrElse(
                    existing -> {
                        // Already exists locally — just update the Keycloak ID if needed
                        if (existing.getKeycloakId() == null) {
                            existing.setKeycloakId(keycloakId);
                            userRepository.save(existing);
                        }
                    },
                    () -> {
                        User newUser = User.builder()
                                .id(UUID.randomUUID())
                                .keycloakId(keycloakId)
                                .email(userDTO.getEmail().toLowerCase().trim())
                                .firstName(userDTO.getFirstName())
                                .lastName(userDTO.getLastName())
                                .birthDate(userDTO.getBirthDate() != null
                                        ? LocalDate.parse(userDTO.getBirthDate())
                                        : null)
                                .country(userDTO.getCountry())
                                .role("client")
                                .createdAt(LocalDateTime.now())
                                .build();
                        userRepository.save(newUser);
                        System.out.println("[registerUser] User saved to local DB: " + userDTO.getEmail());
                    });
        } catch (Exception e) {
            // Don't fail registration if local DB write fails — Keycloak is source of truth
            System.err.println("[registerUser] Failed to save to local DB: " + e.getMessage());
        }
    }

    @Override
    public void forgotPassword(String email) {
        keycloakService.sendForgotPasswordEmail(email);
    }

    @Override
    @Transactional
    public User becomeFreelancer(User user, freelink.user.dto.FreelancerProfileDTO profileDTO) {
        System.out.println("=== SERVICE: becomeFreelancer ===");
        System.out.println("User: " + user.getEmail() + " (ID: " + user.getId() + ")");
        System.out.println("Current role: " + user.getRole());
        System.out.println("ProfileDTO: " + profileDTO);

        // Update the user object directly (it's already managed from syncUser)
        user.setJobTitle(profileDTO.getJobTitle());
        user.setBio(profileDTO.getBio());
        user.setSkills(profileDTO.getSkills());
        user.setHourlyRate(profileDTO.getHourlyRate());
        user.setPortfolioUrl(profileDTO.getPortfolioUrl());

        // ADDITIVE role Change for freelancer
        String currentRole = user.getRole() != null ? user.getRole() : "client";
        Set<String> roles = new java.util.HashSet<>(java.util.Arrays.asList(currentRole.split(",")));
        roles.add("freelancer");
        user.setRole(String.join(",", roles));

        System.out.println("Saving user with role: " + user.getRole());
        User saved = userRepository.save(user);
        System.out.println("User saved successfully with role: " + saved.getRole());

        // Sync role to Keycloak
        if (saved.getKeycloakId() != null) {
            keycloakService.updateUserRole(saved.getKeycloakId(), saved.getRole());
        }

        return saved;
    }

    // --- Notifications ---
    @Override
    public List<Notification> getUserNotifications(UUID userId) {
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    @Override
    public Notification createNotification(UUID userId, String message, String type, String actionUrl) {
        Notification notification = Notification.builder()
                .userId(userId)
                .message(message)
                .type(type)
                .actionUrl(actionUrl)
                .read(false)
                .createdAt(LocalDateTime.now())
                .build();
        return notificationRepository.save(notification);
    }

    @Override
    public void markNotificationAsRead(UUID notificationId) {
        System.out.println(">>> Marking notification [" + notificationId + "] as read");
        notificationRepository.findById(notificationId).ifPresent(notif -> {
            notif.setRead(true);
            notificationRepository.save(notif);
        });
    }
}
