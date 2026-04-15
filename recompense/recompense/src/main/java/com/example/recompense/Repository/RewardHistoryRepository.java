package com.example.recompense.Repository;

import com.example.recompense.Entity.RewardHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RewardHistoryRepository extends JpaRepository<RewardHistory, Long> {

    List<RewardHistory> findByUserEmailOrderByEventDateDesc(String userEmail);

    List<RewardHistory> findAllByOrderByEventDateDesc();

    boolean existsByUserEmailAndRewardNameAndRewardTypeAndActionType(String userEmail,
                                                                     String rewardName,
                                                                     String rewardType,
                                                                     String actionType);
}
