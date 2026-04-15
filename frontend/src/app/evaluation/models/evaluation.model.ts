export interface Evaluation {
  id?: number;
  score: number;
  projectName: string;
  evaluatorName: string;
  userEmail: string;
  evaluatedUserName: string;
  evaluatedUserEmail: string;
  typeEvaluation: string;
  comment?: string;
  anonymous?: boolean;
  date?: string;
  updatedAt?: string;
}

