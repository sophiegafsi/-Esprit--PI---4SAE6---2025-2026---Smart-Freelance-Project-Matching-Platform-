import { Component, DestroyRef, HostListener, Inject, OnInit, PLATFORM_ID, inject } from '@angular/core';
import { isPlatformBrowser } from '@angular/common';
import { Router } from '@angular/router';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { Achievement } from '../../models/portfolio.model';
import { PortfolioService } from '../../services/portfolio.service';
import { PortfolioTheme, PortfolioThemeService } from '../../services/portfolio-theme.service';

type SortKey = 'title' | 'completionDate' | 'freelancerId';
type QuickFilter = 'all' | 'recent' | 'thisYear';

interface FreelancerFilter {
  freelancerId: number;
  count: number;
}

interface PortfolioHeaderStats {
  totalAchievements: number;
  filteredAchievements: number;
  distinctFreelancers: number;
  latestCompletionDate: string;
  recentAchievements: number;
  thisYearAchievements: number;
}

@Component({
  selector: 'app-portfolio-list',
  templateUrl: './portfolio-list.component.html',
  styleUrls: ['./portfolio-list.component.css'],
})
export class PortfolioListComponent implements OnInit {
  private readonly destroyRef = inject(DestroyRef);

  allAchievements: Achievement[] = [];
  achievements: Achievement[] = [];
  loading = false;
  errorMessage = '';
  q = '';
  theme: PortfolioTheme = 'dark';
  quickFilter: QuickFilter = 'all';
  selectedFreelancerId: string | null = null;
  sortKey: SortKey = 'title';
  sortMenuOpen = false;
  sortDir: 'asc' | 'desc' = 'asc';
  page = 0;
  size = 3;
  totalPages = 1;
  stats: PortfolioHeaderStats = {
    totalAchievements: 0,
    filteredAchievements: 0,
    distinctFreelancers: 0,
    latestCompletionDate: '-',
    recentAchievements: 0,
    thisYearAchievements: 0,
  };
  readonly sortOptions: Array<{ key: SortKey; label: string }> = [
    { key: 'title', label: 'Title' },
    { key: 'completionDate', label: 'Completion date' },
  ];
  private autoReloadAttempts = 0;
  private readonly isBrowser: boolean;

  constructor(
    private portfolioService: PortfolioService,
    private router: Router,
    private portfolioThemeService: PortfolioThemeService,
    @Inject(PLATFORM_ID) platformId: object
  ) {
    this.isBrowser = isPlatformBrowser(platformId);
  }

  ngOnInit(): void {
    if (!this.isBrowser) return;
    this.portfolioThemeService.init(this.isBrowser);
    this.theme = this.portfolioThemeService.theme;
    this.portfolioThemeService.theme$
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe((theme) => (this.theme = theme));
    this.load();
  }

  toggleTheme(): void {
    this.portfolioThemeService.toggleTheme();
  }

  closeMenus(): void {
    this.sortMenuOpen = false;
  }

  toggleSortMenu(event: MouseEvent): void {
    event.stopPropagation();
    this.sortMenuOpen = !this.sortMenuOpen;
  }

  selectSort(key: SortKey): void {
    this.sortKey = key;
    this.sortMenuOpen = false;
    this.onSortChange();
  }

  sortLabel(): string {
    return this.sortOptions.find((option) => option.key === this.sortKey)?.label || 'Title';
  }

  setQuickFilter(filter: QuickFilter): void {
    this.quickFilter = filter;
    this.page = 0;
    this.applyView();
  }

  resetFilters(): void {
    this.quickFilter = 'all';
    this.page = 0;
    this.applyView();
  }

  load(): void {
    this.loading = true;
    this.errorMessage = '';

    this.portfolioService.getAchievements().subscribe({
      next: (rows) => {
        this.allAchievements = rows || [];
        this.applyView();
        this.loading = false;
        this.errorMessage = '';
      },
      error: (err) => {
        console.error(err);
        const status = err?.status ? ` (HTTP ${err.status})` : '';
        this.errorMessage = `Unable to load achievements${status}. Check the portfolio-service on port 8090.`;
        this.allAchievements = [];
        this.achievements = [];
        this.totalPages = 1;
        this.updateStats();
        this.loading = false;
      },
    });
  }

  onQueryChange(value: string): void {
    this.q = value ?? '';
    this.page = 0;
    this.applyView();
  }

  onSortChange(): void {
    this.page = 0;
    this.autoReloadAttempts = 0;
    this.applyView();
  }

  filtered(): Achievement[] {
    return this.achievements;
  }

  paged(): Achievement[] {
    const start = this.page * this.size;
    return (this.achievements || []).slice(start, start + this.size);
  }

  prev(): void {
    if (this.page > 0) this.page--;
  }

  next(): void {
    if (this.page + 1 < this.totalPages) this.page++;
  }

  goNew(): void {
    this.router.navigate(['/portfolio/new']);
  }

  goAnalytics(): void {
    this.router.navigate(['/portfolio/analytics']);
  }

  goDetail(id?: number): void {
    if (!id) return;
    this.router.navigate(['/portfolio/detail', id]);
  }

  goEdit(id?: number): void {
    if (!id) return;
    this.router.navigate(['/portfolio/edit', id]);
  }

  remove(id?: number): void {
    if (!id) return;

    const beforeDelete = this.allAchievements.slice();
    this.allAchievements = this.allAchievements.filter((achievement) => achievement.id !== id);
    this.applyView();
    this.errorMessage = '';

    this.portfolioService.deleteAchievement(id).subscribe({
      next: () => this.load(),
      error: (err) => {
        console.error(err);
        this.verifyAchievementDeleted(id, beforeDelete, err);
      },
    });
  }

  private verifyAchievementDeleted(id: number, rollbackRows: Achievement[], err: unknown): void {
    setTimeout(() => {
      this.portfolioService.getAchievements().subscribe({
        next: (rows) => {
          const stillExists = (rows || []).some((achievement) => Number(achievement.id) === id);
          if (!stillExists) {
            this.errorMessage = '';
            this.allAchievements = rows || [];
            this.applyView();
            return;
          }

          this.allAchievements = rollbackRows;
          this.applyView();
          const status = (err as { status?: number } | null)?.status;
          const statusText = status ? ` (HTTP ${status})` : '';
          this.errorMessage = `Delete failed${statusText}. Check portfolio relations and backend routes.`;
        },
        error: (readErr) => {
          console.error(readErr);
          const status = (err as { status?: number } | null)?.status;
          if (status === 0 || status === 500) {
            this.errorMessage = '';
            this.load();
            return;
          }

          this.allAchievements = rollbackRows;
          this.applyView();
          const statusText = status ? ` (HTTP ${status})` : '';
          this.errorMessage = `Delete failed${statusText}. Check portfolio relations and backend routes.`;
        },
      });
    }, 700);
  }

  private applyView(): void {
    this.achievements = this.applyClientFilterAndSort(this.allAchievements);
    this.totalPages = Math.max(1, Math.ceil(this.achievements.length / this.size));
    if (this.page + 1 > this.totalPages) {
      this.page = this.totalPages - 1;
    }
    this.updateStats();
  }

  private applyClientFilterAndSort(rows: Achievement[]): Achievement[] {
    const q = this.q.trim().toLowerCase();
    const filtered = rows
      .filter((achievement) => this.matchesQuickFilter(achievement))
      .filter((achievement) =>
        !q
          ? true
          : [
            achievement.title,
            achievement.description,
            achievement.completionDate,
            String(achievement.id ?? ''),
          ].some((value) => String(value || '').toLowerCase().includes(q))
      )
      .slice();

    const dir = this.sortDir === 'asc' ? 1 : -1;
    filtered.sort((a, b) => {
      if (this.sortKey === 'title') return dir * String(a.title || '').localeCompare(String(b.title || ''), undefined, { sensitivity: 'base' });
      return dir * String(a.completionDate || '').localeCompare(String(b.completionDate || ''));
    });

    return filtered;
  }

  private matchesQuickFilter(achievement: Achievement): boolean {
    if (this.quickFilter === 'recent') return this.isRecentAchievement(achievement);
    if (this.quickFilter === 'thisYear') return this.isCurrentYearAchievement(achievement);
    return true;
  }

  private isRecentAchievement(achievement: Achievement): boolean {
    const achievementDate = this.parseDate(achievement.completionDate);
    if (!achievementDate) return false;

    const now = new Date();
    const diff = now.getTime() - achievementDate.getTime();
    const thirtyDays = 30 * 24 * 60 * 60 * 1000;

    return diff >= 0 && diff <= thirtyDays;
  }

  private isCurrentYearAchievement(achievement: Achievement): boolean {
    const achievementDate = this.parseDate(achievement.completionDate);
    return !!achievementDate && achievementDate.getFullYear() === new Date().getFullYear();
  }

  private updateStats(): void {
    const latestCompletionDate = this.allAchievements
      .map((achievement) => achievement.completionDate || '')
      .filter(Boolean)
      .sort((a, b) => b.localeCompare(a))[0] || '-';

    this.stats = {
      totalAchievements: this.allAchievements.length,
      filteredAchievements: this.achievements.length,
      distinctFreelancers: 0,
      latestCompletionDate,
      recentAchievements: this.allAchievements.filter((achievement) => this.isRecentAchievement(achievement)).length,
      thisYearAchievements: this.allAchievements.filter((achievement) => this.isCurrentYearAchievement(achievement)).length,
    };
  }

  private parseDate(value?: string): Date | undefined {
    const raw = String(value || '').trim();
    if (!raw) return undefined;

    const parsed = new Date(raw);
    return Number.isNaN(parsed.getTime()) ? undefined : parsed;
  }
}
