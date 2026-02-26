import { Component, OnInit } from '@angular/core';
import { EvaluationService } from '../services/evaluation.service';
import { Evaluation } from '../models/evaluation';
import { SentimentService } from '../services/sentiment.service';

@Component({
  selector: 'app-dashboard-client',
  templateUrl: './dashboard-client.component.html',
  styleUrls: ['./dashboard-client.component.css']
})
export class DashboardClientComponent implements OnInit {
  // Statistiques
  totalEvaluations = 0;
  totalFreelancers = 0;
  globalAverage = 0;
  totalProjects = 0;

  // Top freelancers
  topFreelancers: { name: string; averageScore: number; projectCount: number }[] = [];

  // Dernières évaluations
  recentEvaluations: Evaluation[] = [];

  // Statistiques de sentiment
  sentimentStats: { [key: string]: number } = {};
  totalReviews = 0;

  constructor(
    private evaluationService: EvaluationService,
    private sentimentService: SentimentService
  ) {}

  ngOnInit(): void {
    this.loadData();
  }

  loadData(): void {
    this.evaluationService.getEvaluations().subscribe({
      next: (data) => {
        this.calculateStats(data);
        this.recentEvaluations = data.slice(0, 5);
      },
      error: (err) => console.error('Erreur chargement données', err)
    });

    this.sentimentService.getSentimentStats().subscribe({
      next: (stats) => {
        this.sentimentStats = stats;
        this.totalReviews = Object.values(stats).reduce((acc, val) => acc + val, 0);
      },
      error: (err) => console.error('Erreur chargement sentiments', err)
    });
  }

  calculateStats(evaluations: Evaluation[]): void {
    this.totalEvaluations = evaluations.length;

    const freelancerSet = new Set<string>();
    const projectSet = new Set<string>();
    const scoresByFreelancer = new Map<string, { sum: number; count: number }>();

    evaluations.forEach(e => {
      if (e.evaluatedUserName) freelancerSet.add(e.evaluatedUserName);
      if (e.projectName) projectSet.add(e.projectName);
      if (e.evaluatedUserName) {
        const current = scoresByFreelancer.get(e.evaluatedUserName) || { sum: 0, count: 0 };
        current.sum += e.score;
        current.count++;
        scoresByFreelancer.set(e.evaluatedUserName, current);
      }
    });

    this.totalFreelancers = freelancerSet.size;
    this.totalProjects = projectSet.size;

    if (evaluations.length > 0) {
      const totalScore = evaluations.reduce((acc, e) => acc + e.score, 0);
      this.globalAverage = parseFloat((totalScore / evaluations.length).toFixed(2));
    }

    this.topFreelancers = Array.from(scoresByFreelancer.entries())
      .map(([name, data]) => ({
        name,
        averageScore: parseFloat((data.sum / data.count).toFixed(2)),
        projectCount: data.count
      }))
      .sort((a, b) => b.averageScore - a.averageScore)
      .slice(0, 5);
  }

  getRankColor(index: number): string {
    const colors = [
      'linear-gradient(135deg, #f1c40f, #f39c12)',
      'linear-gradient(135deg, #bdc3c7, #95a5a6)',
      'linear-gradient(135deg, #e67e22, #d35400)',
      'linear-gradient(135deg, #3498db, #2980b9)',
      'linear-gradient(135deg, #2ecc71, #27ae60)'
    ];
    return colors[index] || colors[4];
  }

  getPositifPercent(): number {
    return this.totalReviews ? (this.sentimentStats['POSITIF'] || 0) / this.totalReviews * 100 : 0;
  }
  getNeutrePercent(): number {
    return this.totalReviews ? (this.sentimentStats['NEUTRE'] || 0) / this.totalReviews * 100 : 0;
  }
  getNegatifPercent(): number {
    return this.totalReviews ? (this.sentimentStats['NEGATIF'] || 0) / this.totalReviews * 100 : 0;
  }
}