export interface Review {
  id?: number;
  score: number;
  comment: string;
  evaluatorName: string;
  userEmail: string;
  sentiment?: string;
  date?: string;
  evaluation?: {
    id: number;
  };
}

