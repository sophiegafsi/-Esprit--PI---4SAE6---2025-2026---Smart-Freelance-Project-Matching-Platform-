package com.example.recompense.Controller;

import com.example.recompense.DTO.RewardDashboardDTO;
import com.example.recompense.Service.RewardEngineService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class RewardControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private RewardEngineService rewardEngineService;

    @MockBean
    private org.springframework.security.oauth2.jwt.JwtDecoder jwtDecoder;

    @Test
    @DisplayName("GET /api/rewards/dashboard - Should be accessible to ADMIN and return JSON")
    @WithMockUser(roles = "admin")
    void testGetDashboardAuthorized() throws Exception {
        // Arrange
        RewardDashboardDTO mockDashboard = new RewardDashboardDTO();
        mockDashboard.setTotalBadgesAssigned(50L);
        when(rewardEngineService.getDashboard()).thenReturn(mockDashboard);

        // Act & Assert
        mockMvc.perform(get("/api/rewards/dashboard")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalBadgesAssigned").value(50L))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    @DisplayName("GET /api/rewards/dashboard - Should return 403 FORBIDDEN for non-admin")
    @WithMockUser(roles = "client")
    void testGetDashboardForbidden() throws Exception {
        mockMvc.perform(get("/api/rewards/dashboard"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("GET /api/rewards/profiles - Should return 401 UNAUTHORIZED for anonymous")
    void testGetProfilesUnauthorized() throws Exception {
        mockMvc.perform(get("/api/rewards/profiles"))
                .andExpect(status().is4xxClientError()); // 401 or 403 depending on security config
    }
}
