package tn.esprit.reservation;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;
import tn.esprit.reservation.entities.Availability;
import tn.esprit.reservation.repositories.AvailabilityRepository;
import tn.esprit.reservation.services.AvailabilityService;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AvailabilityService Unit Tests")
class AvailabilityServiceTest {

    @Mock
    private AvailabilityRepository availabilityRepository;

    @InjectMocks
    private AvailabilityService availabilityService;

    // ──────────────────────── helpers ────────────────────────

    private Availability buildAvailability(Long id, boolean active,
                                           LocalTime start, LocalTime end,
                                           int maxSlots) {
        return Availability.builder()
                .id(id)
                .freelancerId("freelancer-1")
                .freelancerName("Alice")
                .resourceName("Design")
                .description("UI/UX session")
                .date(LocalDate.of(2025, 6, 15))
                .startTime(start)
                .endTime(end)
                .maxSlots(maxSlots)
                .location("Remote")
                .isActive(active)
                .build();
    }

    // ══════════════════════════════════════════════════════════
    // findAll
    // ══════════════════════════════════════════════════════════
    @Nested
    @DisplayName("findAll()")
    class FindAll {

        @Test
        @DisplayName("returns only active availabilities")
        void shouldReturnActiveAvailabilities() {
            Availability a1 = buildAvailability(1L, true,  LocalTime.of(9, 0), LocalTime.of(10, 0), 3);
            Availability a2 = buildAvailability(2L, true,  LocalTime.of(11, 0), LocalTime.of(12, 0), 5);
            when(availabilityRepository.findByIsActiveTrue()).thenReturn(List.of(a1, a2));

            List<Availability> result = availabilityService.findAll();

            assertThat(result).hasSize(2).containsExactly(a1, a2);
            verify(availabilityRepository, times(1)).findByIsActiveTrue();
        }

        @Test
        @DisplayName("returns empty list when no active availabilities")
        void shouldReturnEmptyListWhenNoneActive() {
            when(availabilityRepository.findByIsActiveTrue()).thenReturn(List.of());

            assertThat(availabilityService.findAll()).isEmpty();
        }
    }

    // ══════════════════════════════════════════════════════════
    // findAllIncludingInactive
    // ══════════════════════════════════════════════════════════
    @Nested
    @DisplayName("findAllIncludingInactive()")
    class FindAllIncludingInactive {

        @Test
        @DisplayName("delegates to repository.findAll()")
        void shouldDelegateToFindAll() {
            Availability active   = buildAvailability(1L, true,  LocalTime.of(9,  0), LocalTime.of(10, 0), 2);
            Availability inactive = buildAvailability(2L, false, LocalTime.of(11, 0), LocalTime.of(12, 0), 2);
            when(availabilityRepository.findAll()).thenReturn(List.of(active, inactive));

            List<Availability> result = availabilityService.findAllIncludingInactive();

            assertThat(result).hasSize(2);
            verify(availabilityRepository).findAll();
        }
    }

    // ══════════════════════════════════════════════════════════
    // findById
    // ══════════════════════════════════════════════════════════
    @Nested
    @DisplayName("findById()")
    class FindById {

        @Test
        @DisplayName("returns the availability when found")
        void shouldReturnAvailabilityWhenExists() {
            Availability av = buildAvailability(10L, true, LocalTime.of(8, 0), LocalTime.of(9, 0), 1);
            when(availabilityRepository.findById(10L)).thenReturn(Optional.of(av));

            Optional<Availability> result = availabilityService.findById(10L);

            assertThat(result).isPresent().contains(av);
        }

        @Test
        @DisplayName("returns empty when not found")
        void shouldReturnEmptyWhenNotFound() {
            when(availabilityRepository.findById(99L)).thenReturn(Optional.empty());

            assertThat(availabilityService.findById(99L)).isEmpty();
        }
    }

    // ══════════════════════════════════════════════════════════
    // findByDate
    // ══════════════════════════════════════════════════════════
    @Nested
    @DisplayName("findByDate()")
    class FindByDate {

        @Test
        @DisplayName("delegates with the supplied date")
        void shouldDelegateWithDate() {
            LocalDate date = LocalDate.of(2025, 7, 1);
            Availability av = buildAvailability(3L, true, LocalTime.of(10, 0), LocalTime.of(11, 0), 4);
            when(availabilityRepository.findByDateAndIsActiveTrue(date)).thenReturn(List.of(av));

            assertThat(availabilityService.findByDate(date)).containsExactly(av);
        }
    }

    // ══════════════════════════════════════════════════════════
    // findByDateRange
    // ══════════════════════════════════════════════════════════
    @Nested
    @DisplayName("findByDateRange()")
    class FindByDateRange {

        @Test
        @DisplayName("delegates with start and end dates")
        void shouldDelegateWithDateRange() {
            LocalDate start = LocalDate.of(2025, 7, 1);
            LocalDate end   = LocalDate.of(2025, 7, 31);
            Availability av = buildAvailability(4L, true, LocalTime.of(9, 0), LocalTime.of(10, 0), 2);
            when(availabilityRepository.findByDateBetweenAndIsActiveTrue(start, end)).thenReturn(List.of(av));

            assertThat(availabilityService.findByDateRange(start, end)).containsExactly(av);
        }
    }

    // ══════════════════════════════════════════════════════════
    // searchByResource
    // ══════════════════════════════════════════════════════════
    @Nested
    @DisplayName("searchByResource()")
    class SearchByResource {

        @Test
        @DisplayName("delegates to case-insensitive repository method")
        void shouldDelegateCaseInsensitiveSearch() {
            Availability av = buildAvailability(5L, true, LocalTime.of(14, 0), LocalTime.of(15, 0), 3);
            when(availabilityRepository.findByResourceNameContainingIgnoreCaseAndIsActiveTrue("design"))
                    .thenReturn(List.of(av));

            assertThat(availabilityService.searchByResource("design")).containsExactly(av);
        }
    }

    // ══════════════════════════════════════════════════════════
    // findByFreelancerId
    // ══════════════════════════════════════════════════════════
    @Nested
    @DisplayName("findByFreelancerId()")
    class FindByFreelancerId {

        @Test
        @DisplayName("returns active availabilities for the freelancer")
        void shouldReturnAvailabilitiesForFreelancer() {
            Availability av = buildAvailability(6L, true, LocalTime.of(10, 0), LocalTime.of(11, 0), 2);
            when(availabilityRepository.findByFreelancerIdAndIsActiveTrue("freelancer-1")).thenReturn(List.of(av));

            assertThat(availabilityService.findByFreelancerId("freelancer-1")).containsExactly(av);
        }
    }

    // ══════════════════════════════════════════════════════════
    // create
    // ══════════════════════════════════════════════════════════
    @Nested
    @DisplayName("create()")
    class Create {

        @Test
        @DisplayName("saves and returns availability with valid data")
        void shouldCreateAvailabilitySuccessfully() {
            Availability input = buildAvailability(null, true, LocalTime.of(9, 0), LocalTime.of(10, 0), 3);
            Availability saved = buildAvailability(1L,   true, LocalTime.of(9, 0), LocalTime.of(10, 0), 3);
            when(availabilityRepository.save(input)).thenReturn(saved);

            Availability result = availabilityService.create(input);

            assertThat(result.getId()).isEqualTo(1L);
            verify(availabilityRepository).save(input);
        }

        @Test
        @DisplayName("throws 400 when startTime is after endTime")
        void shouldThrowWhenStartAfterEnd() {
            Availability bad = buildAvailability(null, true, LocalTime.of(11, 0), LocalTime.of(9, 0), 3);

            assertThatThrownBy(() -> availabilityService.create(bad))
                    .isInstanceOf(ResponseStatusException.class)
                    .hasMessageContaining("Start time must be before end time");

            verify(availabilityRepository, never()).save(any());
        }

        @Test
        @DisplayName("throws 400 when maxSlots is zero")
        void shouldThrowWhenMaxSlotsIsZero() {
            Availability bad = buildAvailability(null, true, LocalTime.of(9, 0), LocalTime.of(10, 0), 0);

            assertThatThrownBy(() -> availabilityService.create(bad))
                    .isInstanceOf(ResponseStatusException.class)
                    .hasMessageContaining("Max slots must be positive");

            verify(availabilityRepository, never()).save(any());
        }

        @Test
        @DisplayName("throws 400 when maxSlots is negative")
        void shouldThrowWhenMaxSlotsIsNegative() {
            Availability bad = buildAvailability(null, true, LocalTime.of(9, 0), LocalTime.of(10, 0), -1);

            assertThatThrownBy(() -> availabilityService.create(bad))
                    .isInstanceOf(ResponseStatusException.class)
                    .hasMessageContaining("Max slots must be positive");
        }

        @Test
        @DisplayName("accepts availability whose start equals end — boundary")
        void shouldThrowWhenStartEqualsEnd() {
            // start == end is NOT after end, so it should NOT throw the time guard
            // but business-wise it is still valid by current code (no equal check)
            Availability input = buildAvailability(null, true, LocalTime.of(9, 0), LocalTime.of(9, 0), 2);
            Availability saved = buildAvailability(7L,   true, LocalTime.of(9, 0), LocalTime.of(9, 0), 2);
            when(availabilityRepository.save(input)).thenReturn(saved);

            Availability result = availabilityService.create(input);
            assertThat(result).isNotNull();
        }
    }

    // ══════════════════════════════════════════════════════════
    // update
    // ══════════════════════════════════════════════════════════
    @Nested
    @DisplayName("update()")
    class Update {

        @Test
        @DisplayName("updates every field and saves when found")
        void shouldUpdateAllFields() {
            Availability existing = buildAvailability(1L, true, LocalTime.of(9, 0), LocalTime.of(10, 0), 3);
            Availability payload  = Availability.builder()
                    .freelancerId("freelancer-2")
                    .freelancerName("Bob")
                    .resourceName("DevOps")
                    .description("CI/CD session")
                    .date(LocalDate.of(2025, 8, 1))
                    .startTime(LocalTime.of(14, 0))
                    .endTime(LocalTime.of(15, 0))
                    .maxSlots(5)
                    .location("Onsite")
                    .isActive(false)
                    .build();

            when(availabilityRepository.findById(1L)).thenReturn(Optional.of(existing));
            when(availabilityRepository.save(existing)).thenReturn(existing);

            Availability result = availabilityService.update(1L, payload);

            assertThat(result.getFreelancerName()).isEqualTo("Bob");
            assertThat(result.getMaxSlots()).isEqualTo(5);
            assertThat(result.getIsActive()).isFalse();
            verify(availabilityRepository).save(existing);
        }

        @Test
        @DisplayName("throws RuntimeException when availability not found")
        void shouldThrowWhenNotFound() {
            when(availabilityRepository.findById(99L)).thenReturn(Optional.empty());
            Availability payload = buildAvailability(null, true, LocalTime.of(9, 0), LocalTime.of(10, 0), 2);

            assertThatThrownBy(() -> availabilityService.update(99L, payload))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("Availability not found with id: 99");
        }
    }

    // ══════════════════════════════════════════════════════════
    // delete (soft-delete)
    // ══════════════════════════════════════════════════════════
    @Nested
    @DisplayName("delete() — soft delete")
    class Delete {

        @Test
        @DisplayName("sets isActive=false and saves when found")
        void shouldSoftDeleteWhenFound() {
            Availability av = buildAvailability(1L, true, LocalTime.of(9, 0), LocalTime.of(10, 0), 3);
            when(availabilityRepository.findById(1L)).thenReturn(Optional.of(av));

            availabilityService.delete(1L);

            assertThat(av.getIsActive()).isFalse();
            verify(availabilityRepository).save(av);
        }

        @Test
        @DisplayName("does nothing when availability not found")
        void shouldDoNothingWhenNotFound() {
            when(availabilityRepository.findById(99L)).thenReturn(Optional.empty());

            availabilityService.delete(99L);

            verify(availabilityRepository, never()).save(any());
        }
    }
}
