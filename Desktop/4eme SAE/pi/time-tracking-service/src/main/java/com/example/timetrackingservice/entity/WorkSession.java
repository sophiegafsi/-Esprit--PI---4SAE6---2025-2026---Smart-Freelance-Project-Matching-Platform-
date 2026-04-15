package com.example.timetrackingservice.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "work_sessions")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkSession {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private UUID contractId;

    @Column(nullable = false)
    private UUID freelancerId;

    private LocalDateTime startTime;
    private LocalDateTime endTime;

    private Long totalMinutesWorked;

    @Enumerated(EnumType.STRING)
    private SessionStatus status;

    @OneToMany(mappedBy = "workSession", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private List<WorkSnapshot> snapshots;

    @PrePersist
    protected void onCreate() {
        if (this.startTime == null) {
            this.startTime = LocalDateTime.now();
        }
        if (this.status == null) {
            this.status = SessionStatus.ACTIVE;
        }
        if (this.totalMinutesWorked == null) {
            this.totalMinutesWorked = 0L;
        }
    }
}
