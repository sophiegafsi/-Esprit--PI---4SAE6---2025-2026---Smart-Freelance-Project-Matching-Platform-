export enum TypeEvaluation {
  TECHNICAL = 'TECHNICAL',
  SOFT = 'SOFT',
  OTHER = 'OTHER'
}
export interface Evaluation {
  id?: number;
  projectName?: string;
  evaluatorName?: string;
  evaluatedUserName?: string;
  score: number;
  comment?: string;
  anonymous: boolean;
  date?: string;
  typeEvaluation?: string;
}