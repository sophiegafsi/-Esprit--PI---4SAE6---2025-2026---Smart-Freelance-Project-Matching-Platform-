export interface Review {
  id?: number;
  score: number;           // ← ajouté
  comment: string;
  evaluatorName?: string;  // ← ajouté (optionnel)
  date?: string;           // ← ajouté (optionnel)
  evaluation?: {
    idE: number;
  };
}