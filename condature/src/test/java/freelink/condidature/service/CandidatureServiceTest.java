package freelink.condidature.service;

import freelink.condidature.entity.Candidature;
import freelink.condidature.repository.CandidatureRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestTemplate;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CandidatureServiceTest {

    @Mock
    private CandidatureRepository candidatureRepository;

    @Mock
    private LanguageToolService languageToolService;

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private CandidatureService candidatureService;

    @Test
    @DisplayName("Should allow updating cover letter for pending application by owner")
    void testUpdateCoverLetterSuccess() {
        // Arrange
        UUID candidatureId = UUID.randomUUID();
        UUID freelancerId = UUID.randomUUID();
        Candidature candidature = Candidature.builder()
                .id(candidatureId)
                .freelancerId(freelancerId)
                .status(Candidature.Status.PENDING)
                .build();

        when(candidatureRepository.findById(candidatureId)).thenReturn(Optional.of(candidature));
        when(candidatureRepository.save(any(Candidature.class))).thenAnswer(i -> i.getArguments()[0]);

        // Act
        Candidature updated = candidatureService.updateCoverLetter(candidatureId, freelancerId, "New Letter");

        // Assert
        assertEquals("New Letter", updated.getCoverLetter());
        verify(candidatureRepository).save(any());
    }

    @Test
    @DisplayName("Should throw exception when unauthorized user tries to update cover letter")
    void testUpdateCoverLetterUnauthorized() {
        UUID candidatureId = UUID.randomUUID();
        UUID ownerId = UUID.randomUUID();
        UUID otherId = UUID.randomUUID();
        
        Candidature candidature = Candidature.builder()
                .id(candidatureId)
                .freelancerId(ownerId)
                .status(Candidature.Status.PENDING)
                .build();

        when(candidatureRepository.findById(candidatureId)).thenReturn(Optional.of(candidature));

        assertThrows(RuntimeException.class, () -> candidatureService.updateCoverLetter(candidatureId, otherId, "Hack"));
    }

    @Test
    @DisplayName("Should throw exception when trying to update non-pending application")
    void testUpdateCoverLetterNotPending() {
        UUID candidatureId = UUID.randomUUID();
        UUID freelancerId = UUID.randomUUID();
        
        Candidature candidature = Candidature.builder()
                .id(candidatureId)
                .freelancerId(freelancerId)
                .status(Candidature.Status.ACCEPTED)
                .build();

        when(candidatureRepository.findById(candidatureId)).thenReturn(Optional.of(candidature));

        assertThrows(RuntimeException.class, () -> candidatureService.updateCoverLetter(candidatureId, freelancerId, "Too late"));
    }

    @Test
    void testRejectApplication_ShouldUpdateStatus() {
        UUID candidatureId = UUID.randomUUID();
        UUID clientId = UUID.randomUUID();
        Candidature candidature = Candidature.builder()
                .id(candidatureId)
                .freelancerId(UUID.randomUUID())
                .status(Candidature.Status.PENDING)
                .build();
        
        when(candidatureRepository.findById(candidatureId)).thenReturn(Optional.of(candidature));
        when(candidatureRepository.save(any(Candidature.class))).thenReturn(candidature);
        
        Candidature result = candidatureService.rejectApplication(candidatureId, clientId);
        
        assertEquals(Candidature.Status.REJECTED, result.getStatus());
        verify(candidatureRepository).save(candidature);
    }
}
