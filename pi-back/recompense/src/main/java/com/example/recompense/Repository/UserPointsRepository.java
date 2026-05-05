package com.example.recompense.Repository;

import com.example.recompense.Entity.UserPoints;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserPointsRepository extends JpaRepository<UserPoints, Long> {

    UserPoints findByUserEmail(String userEmail);

    Optional<UserPoints> findOptionalByUserEmail(String userEmail);

    @Query("SELECT COALESCE(SUM(u.points),0) FROM UserPoints u WHERE u.userEmail = :email")
    int getTotalPoints(@Param("email") String email);
}
