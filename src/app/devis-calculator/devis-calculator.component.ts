// src/app/devis-calculator/devis-calculator.component.ts
import { Component, Input, OnChanges, SimpleChanges } from '@angular/core';
import { CommonModule } from '@angular/common';
import { DevisCalculatorService, DevisResult } from './devis-calculator.service';

@Component({
  selector: 'app-devis-calculator',
  standalone: true,
  imports: [CommonModule],
  template: `
    <!-- Loading -->
    <div class="devis-card" *ngIf="isLoading">
      <div class="devis-header">
        <h3>💰 Budget</h3>
      </div>
      <p class="muted">Calcul du devis en cours...</p>
    </div>

    <!-- Error -->
    <div class="devis-card" *ngIf="!isLoading && error">
      <div class="devis-header">
        <h3>💰 Budget</h3>
      </div>
      <p class="error">{{ error }}</p>
    </div>

    <!-- Result -->
    <div class="devis-card" *ngIf="!isLoading && devis">
      <div class="devis-header">
        <div class="left">
          <h3>💰 Budget</h3>
        </div>
        <span class="confidence" [style.background]="getConfidenceColor()">
          Confiance {{ devis.confiance }}%
        </span>
      </div>

      <div class="price-container">
        <div class="price-main">
          <span class="currency">€</span>
          <span class="amount">{{ devis.prixRecommande }}</span>
        </div>
        <div class="price-range">
          {{ devis.prixMinimum }}€ <span class="dot">•</span> {{ devis.prixMaximum }}€
        </div>
      </div>

      <div class="details-grid">
        <div class="detail-item">
          <span class="label">⏱️ Heures</span>
          <span class="value">{{ devis.heuresEstimees }}h</span>
        </div>

        <div class="detail-item">
          <span class="label">💰 Taux/h</span>
          <span class="value">{{ devis.tauxHoraire }}€</span>
        </div>

        <div class="detail-item">
          <span class="label">📊 Complexité</span>
          <span class="value">x{{ devis.facteurComplexite }}</span>
        </div>

        <div class="detail-item" *ngIf="devis.facteurUrgence && devis.facteurUrgence !== 1">
          <span class="label">⚡ Urgence</span>
          <span class="value">x{{ devis.facteurUrgence }}</span>
        </div>

        <div class="detail-item" *ngIf="devis.joursDisponibles">
          <span class="label">📅 Jours</span>
          <span class="value">{{ devis.joursDisponibles }}</span>
        </div>
      </div>

      <div class="breakdown" *ngIf="devis.decomposition?.length">
        <h4>Décomposition</h4>
        <div class="row" *ngFor="let item of devis.decomposition">
          <span class="poste">{{ item.poste }} ({{ item.heures }}h)</span>
          <span class="montant">{{ item.montant }}€</span>
        </div>
      </div>

      <div class="recommandation">
        {{ devis.recommandation }}
      </div>
    </div>
  `,
  styles: [`
    .devis-card{
      background: linear-gradient(135deg, rgba(10,30,50,.92), rgba(8,18,28,.92));
      border-radius: 18px;
      padding: 22px;
      margin: 18px 0;
      border: 1px solid rgba(255,255,255,.10);
      color: white;
      box-shadow: 0 18px 44px rgba(0,0,0,.25);
    }
    .devis-header{
      display:flex; justify-content:space-between; align-items:center; gap:12px;
      margin-bottom: 16px;
    }
    .left{ display:flex; flex-direction:column; gap:2px; }
    .devis-header h3{ margin:0; font-size:20px; font-weight:900; color:#f2994a; }
    .subtitle{ color: rgba(255,255,255,.6); font-weight:800; }

    .confidence{
      padding: 8px 14px;
      border-radius: 999px;
      font-size: 12px;
      font-weight: 900;
      color: white;
      border: 1px solid rgba(255,255,255,.18);
      white-space: nowrap;
    }

    .price-container{
      text-align:center;
      padding: 18px;
      border-radius: 14px;
      background: radial-gradient(1200px 120px at 50% 0%, rgba(242,153,74,.22), rgba(255,255,255,0));
      border: 1px solid rgba(255,255,255,.08);
      margin-bottom: 14px;
    }
    .price-main{ display:flex; justify-content:center; align-items:baseline; gap:8px; }
    .currency{ font-size:22px; color: rgba(255,255,255,.65); font-weight:900; }
    .amount{ font-size:58px; font-weight:1000; color:#f2994a; line-height:1; letter-spacing: .5px; }
    .price-range{ margin-top: 8px; color: rgba(255,255,255,.65); font-weight:900; }
    .dot{ margin: 0 8px; opacity:.7; }

    .details-grid{
      display:grid;
      grid-template-columns: repeat(5, 1fr);
      gap: 12px;
      margin-bottom: 14px;
    }
    .detail-item{
      padding: 12px;
      border-radius: 14px;
      background: rgba(255,255,255,.04);
      border: 1px solid rgba(255,255,255,.07);
      display:flex; flex-direction:column; gap:6px;
      min-height: 64px;
    }
    .label{ font-size:12px; color: rgba(255,255,255,.65); font-weight:900; }
    .value{ font-size:16px; font-weight:1000; }

    .breakdown{
      padding: 16px;
      border-radius: 14px;
      background: rgba(255,255,255,.03);
      border: 1px solid rgba(255,255,255,.08);
      margin-bottom: 14px;
    }
    .breakdown h4{ margin:0 0 10px; font-size:13px; font-weight:1000; opacity:.9; }
    .row{ display:flex; justify-content:space-between; padding:10px 0; border-bottom:1px solid rgba(255,255,255,.06); }
    .row:last-child{ border-bottom:none; }
    .poste{ font-weight:900; color: rgba(255,255,255,.82); }
    .montant{ font-weight:1000; color:#f2994a; }

    .recommandation{
      padding: 14px;
      border-radius: 12px;
      background: rgba(242,153,74,.10);
      border: 1px solid rgba(242,153,74,.22);
      font-weight: 900;
    }

    .muted{ color: rgba(255,255,255,.65); font-weight:900; }
    .error{ color: #ff6b6b; font-weight:1000; }

    @media (max-width: 1000px){
      .details-grid{ grid-template-columns: repeat(2, 1fr); }
    }
  `]
})
export class DevisCalculatorComponent implements OnChanges {
  @Input() projet: any = null;
  @Input() deadline?: string;

  // ✅ valeurs préchargées depuis ProjetDetailComponent
  @Input() prefetchedDevis: DevisResult | null = null;
  @Input() prefetchedLoading: boolean | null = null;
  @Input() prefetchedError: string | null = null;

  devis: DevisResult | null = null;
  isLoading = false;
  error: string | null = null;

  constructor(private devisService: DevisCalculatorService) {}

  ngOnChanges(changes: SimpleChanges): void {
    // 1) si le parent donne déjà le devis => afficher direct
    if (changes['prefetchedDevis'] || changes['prefetchedLoading'] || changes['prefetchedError']) {
      this.devis = this.prefetchedDevis;
      this.isLoading = !!this.prefetchedLoading;
      this.error = this.prefetchedError;
      return;
    }

    // 2) sinon fallback: appel direct (avec cache service)
    const projetChanged = !!changes['projet'];
    const deadlineChanged = !!changes['deadline'];
    if (!projetChanged && !deadlineChanged) return;

    if (!this.projet || !this.projet.id) {
      this.devis = null;
      this.error = null;
      this.isLoading = false;
      return;
    }

    this.fetchDevis();
  }

  private fetchDevis(): void {
    this.isLoading = true;
    this.error = null;
    this.devis = null;

    this.devisService.calculerDevisDepuisBackend(this.projet.id, this.deadline).subscribe({
      next: (res) => {
        this.devis = res;
        this.isLoading = false;
      },
      error: (e: Error) => {
        this.error = e.message || 'Erreur lors du calcul du devis.';
        this.isLoading = false;
      }
    });
  }

  getConfidenceColor(): string {
    if (!this.devis) return '#95a5a6';
    if (this.devis.confiance >= 80) return '#2ecc71';
    if (this.devis.confiance >= 60) return '#f39c12';
    return '#e74c3c';
  }
}
