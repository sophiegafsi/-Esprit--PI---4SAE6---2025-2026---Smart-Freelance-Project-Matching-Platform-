// src/app/models/projet.ts
export interface Projet {
  id?: number;
  title: string;
  description: string;
  date: string;
  domaine: string;
  clientId?: string;
  budget?: number;
  status?: string;
  tasks?: ProjetDetaille[];
}


export interface ProjetDetaille {
  id?: number;
  taskname: string;
  description: string;
  deadline: string;
  projet?: Projet;
}
export type Task = ProjetDetaille;
