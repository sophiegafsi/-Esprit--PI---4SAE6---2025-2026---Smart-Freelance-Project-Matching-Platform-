import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormationService, Formation } from '../../services/formation.service';

@Component({
  selector: 'app-formations',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './formations.html',
  styleUrls: ['./formations.css']
})
export class FormationsComponent implements OnInit {
  formations: Formation[] = [];
  loading: boolean = true;
  errorMessage: string = '';

  constructor(private formationService: FormationService) {}

  ngOnInit(): void {
    console.log('🟡 Chargement des formations...');
    this.formationService.getAll().subscribe({
      next: (data) => {
        console.log('✅ Données reçues:', data);
        this.formations = data;
        this.loading = false;
      },
      error: (err) => {
        console.error('❌ Erreur API:', err);
        this.errorMessage = 'Erreur: impossible de charger les formations';
        this.loading = false;
      },
      complete: () => {
        console.log('✅ Requête terminée');
      }
    });
  }
}
