export interface Badge {
  id?: number;
  name: string;
  description?: string;
  icon?: string;
  category?: string;
  conditionType?: string;
  conditionValue?: number;
  pointsReward?: number;
  autoAssignable?: boolean;
  certificateEligible?: boolean;
  isActive?: boolean;
  createdAt?: string;
}
