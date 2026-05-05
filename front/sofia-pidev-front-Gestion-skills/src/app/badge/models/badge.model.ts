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

export interface UserBadgeDTO {
  badgeName: string;
  description: string;
  icon: string;
  dateAssigned: string | Date;
  active: boolean;
  statusReason?: string;
  certificateGenerated: boolean;
  conditionType: string;
  conditionValue: number;
}
