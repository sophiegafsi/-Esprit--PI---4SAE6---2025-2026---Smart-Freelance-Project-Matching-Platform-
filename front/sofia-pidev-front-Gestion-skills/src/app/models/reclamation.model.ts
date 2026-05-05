export enum Priorite {
  BASSE = 'BASSE',
  MOYENNE = 'MOYENNE',
  HAUTE = 'HAUTE',
  CRITIQUE = 'CRITIQUE'
}

export enum Statut {
  EN_ATTENTE = 'EN_ATTENTE',
  EN_COURS = 'EN_COURS',
  RESOLUE = 'RESOLUE',
  REJETEE = 'REJETEE'
}

export enum Type {
  PROJET = 'PROJET',
  PAIEMENT = 'PAIEMENT',
  UTILISATEUR = 'UTILISATEUR',
  TECHNIQUE = 'TECHNIQUE'
}

export interface Reponse {
  idReponse?: number;
  message: string;
  dateReponse?: Date;
  utilisateur: string;
  reclamation?: Reclamation;
}

export interface Reclamation {
  idReclamation?: number;
  sujet: string;
  description: string;
  dateCreation?: Date;
  statut: Statut;
  priorite: Priorite;
  type: Type;
  reponses?: Reponse[];

  // 🔥 AJOUT IMPORTANT
  urgent?: boolean;
  urgentReason?: string;
  idUtilisateur?: string;
  idCible?: string;
}
