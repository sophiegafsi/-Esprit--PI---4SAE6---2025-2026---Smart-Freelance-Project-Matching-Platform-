package freelink.user.service;

import freelink.user.dto.UserDTO;
import jakarta.ws.rs.core.Response;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.keycloak.admin.client.CreatedResponseUtil;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
public class KeycloakService {

    @Value("${keycloak.auth-server-url:http://localhost:8080}")
    private String serverUrl;

    @Value("${keycloak.realm:freelink-realm}")
    private String realm;

    @Value("${keycloak.resource:user-service}")
    private String clientId;

    @Value("${keycloak.credentials.secret:secret}") // Needs to be configured in application.properties
    private String clientSecret;

    private Keycloak keycloak;

    // Lazy initialization or @PostConstruct to build the client
    private Keycloak getKeycloakInstance() {
        if (keycloak == null) {
            keycloak = KeycloakBuilder.builder()
                    .serverUrl(serverUrl)
                    .realm(realm)
                    .grantType("client_credentials")
                    .clientId(clientId)
                    .clientSecret(clientSecret)
                    .build();
        }
        return keycloak;
    }

    public void createUser(UserDTO userDTO) {
        UserRepresentation user = new UserRepresentation();
        user.setEnabled(true);
        user.setUsername(userDTO.getEmail());
        user.setEmail(userDTO.getEmail());
        user.setFirstName(userDTO.getFirstName());
        user.setLastName(userDTO.getLastName());
        user.setEmailVerified(false); // require email verification if needed

        if (userDTO.getBirthDate() != null) {
            user.singleAttribute("birthDate", userDTO.getBirthDate());
        }
        if (userDTO.getCountry() != null) {
            user.singleAttribute("country", userDTO.getCountry());
        }

        CredentialRepresentation credential = new CredentialRepresentation();
        credential.setType(CredentialRepresentation.PASSWORD);
        credential.setValue(userDTO.getPassword());
        credential.setTemporary(false);

        user.setCredentials(Collections.singletonList(credential));

        UsersResource usersResource = getKeycloakInstance().realm(realm).users();
        try (Response response = usersResource.create(user)) {
            if (response.getStatus() == 201) {
                // User created successfully
                String userId = CreatedResponseUtil.getCreatedId(response);
                System.out.println("User created in Keycloak with ID: " + userId);

                // Trigger email verification safely
                try {
                    usersResource.get(userId).executeActionsEmail(Collections.singletonList("VERIFY_EMAIL"));
                    System.out.println("Verification email sent to: " + userDTO.getEmail());
                } catch (Exception e) {
                    System.err.println("Failed to send verification email: " + e.getMessage());
                    // Don't fail the registration, just log it.
                }

                // Assign 'client' role
                try {
                    org.keycloak.representations.idm.RoleRepresentation clientRole = getKeycloakInstance()
                            .realm(realm)
                            .roles()
                            .get("client")
                            .toRepresentation();
                    usersResource.get(userId).roles().realmLevel().add(Collections.singletonList(clientRole));
                    System.out.println("Assigned 'client' role to user: " + userDTO.getEmail());
                } catch (Exception e) {
                    System.err.println(
                            "Failed to assign 'client' role (ensure role exists in Keycloak): " + e.getMessage());
                }

            } else if (response.getStatus() == 409) {
                System.err.println("User already exists: " + userDTO.getEmail());
                throw new RuntimeException("User already exists");
            } else {
                System.err.println("Failed to create user. Status: " + response.getStatus());
                throw new RuntimeException("Failed to create user in Keycloak. Status: " + response.getStatus());
            }
        } catch (Exception e) {
            System.err.println("Error in createUser: " + e.getMessage());
            throw new RuntimeException("Error creating user: " + e.getMessage(), e);
        }
    }

    public void updateUser(String keycloakId, String firstName, String lastName) {
        if (keycloakId == null || keycloakId.isEmpty())
            return;
        try {
            UsersResource usersResource = getKeycloakInstance().realm(realm).users();
            UserRepresentation user = usersResource.get(keycloakId).toRepresentation();
            user.setFirstName(firstName);
            user.setLastName(lastName);
            usersResource.get(keycloakId).update(user);
            System.out.println("User updated in Keycloak: " + keycloakId);
        } catch (Exception e) {
            System.err.println("Failed to update user in Keycloak: " + e.getMessage());
        }
    }

    public void deleteUser(String keycloakId) {
        if (keycloakId == null || keycloakId.isEmpty())
            return;
        try {
            UsersResource usersResource = getKeycloakInstance().realm(realm).users();
            usersResource.get(keycloakId).remove();
            System.out.println("User deleted from Keycloak: " + keycloakId);
        } catch (Exception e) {
            System.err.println("Failed to delete user from Keycloak: " + e.getMessage());
        }
    }

    public void sendForgotPasswordEmail(String email) {
        UsersResource usersResource = getKeycloakInstance().realm(realm).users();
        List<UserRepresentation> users = usersResource.searchByEmail(email, true);
        if (users != null && !users.isEmpty()) {
            UserRepresentation user = users.get(0);
            usersResource.get(user.getId()).executeActionsEmail(Collections.singletonList("UPDATE_PASSWORD"));
        } else {
            // Do not disclose if user exists or not, or log it
        }
    }
}
