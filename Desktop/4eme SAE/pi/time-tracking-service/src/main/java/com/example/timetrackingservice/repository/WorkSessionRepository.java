package com.example.timetrackingservice.repository;

import com.example.timetrackingservice.entity.WorkSession;
import com.example.timetrackingservice.entity.SessionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface WorkSessionRepository extends JpaRepository<WorkSession, UUID> {
    List<WorkSession> findByContractId(UUID contractId);
    List<WorkSession> findByStatus(SessionStatus status);
}
