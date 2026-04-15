export interface RewardHistoryItem {
  id: number;
  userEmail?: string;
  userName?: string;
  rewardType?: string;
  rewardName?: string;
  actionType?: string;
  averageScoreSnapshot?: number;
  totalPointsSnapshot?: number;
  certificateGenerated?: boolean;
  eventDate?: string;
}

