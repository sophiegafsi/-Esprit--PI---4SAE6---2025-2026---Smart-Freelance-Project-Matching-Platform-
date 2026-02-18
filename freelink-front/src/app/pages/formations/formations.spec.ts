import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { FormationService } from '../../services/formation.service';

@Component({
  selector: 'app-formations',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './formations.html',
  styleUrls: ['./formations.css'],
})
export class FormationsComponent implements OnInit {
  formations: any[] = [];
  loading = false;

  constructor(private formationService: FormationService) {}

  ngOnInit(): void {
    this.loadFormations();
  }

  loadFormations() {
    this.loading = true;
    this.formationService.getAll().subscribe({
      next: (res: any) => {
        this.formations = res;
        this.loading = false;
      },
      error: (err) => {
        console.error(err);
        this.loading = false;
        alert('Erreur: impossible de charger les formations. (Voir console F12)');
      },
    });
  }

  deleteFormation(id: number) {
    const ok = confirm('Voulez-vous supprimer cette formation ?');
    if (!ok) return;

    this.formationService.delete(id).subscribe({
      next: () => {
        // refresh list
        this.loadFormations();
      },
      error: (err) => {
        console.error(err);
        alert('Erreur: suppression impossible. (Voir console F12)');
      },
    });
  }
}
