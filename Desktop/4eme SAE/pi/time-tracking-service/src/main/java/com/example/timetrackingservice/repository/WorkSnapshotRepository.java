package com.example.timetrackingservice.repository;

import com.example.timetrackingservice.entity.WorkSession;
import com.example.timetrackingservice.entity.WorkSnapshot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface WorkSnapshotRepository extends JpaRepository<WorkSnapshot, UUID> {
    @Modifying
    @Transactional
    void deleteByTimestampBefore(LocalDateTime timestamp);

    List<WorkSnapshot> findByWorkSession(WorkSession workSession);
}
