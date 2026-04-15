package com.example.recompense.Repository;

import com.example.recompense.Entity.Badge;
import com.example.recompense.Entity.UserBadge;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface UserBadgeRepository extends JpaRepository<UserBadge, Long> {

    boolean existsByUserNameAndBadgeAndActiveTrue(String userName, Badge badge);

    List<UserBadge> findByUserName(String userName);

    List<UserBadge> findByUserNameOrderByDateAssignedDesc(String userName);

    List<UserBadge> findByUserNameAndActiveTrueOrderByDateAssignedDesc(String userName);

    @Query("SELECT ub FROM UserBadge ub JOIN FETCH ub.badge WHERE ub.userName = :userName")
    List<UserBadge> findByUserNameWithBadge(@Param("userName") String userName);

    long countByActiveTrue();
}
