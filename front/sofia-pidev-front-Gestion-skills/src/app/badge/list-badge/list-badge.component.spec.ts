import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ListBadgeComponent } from './list-badge.component';
import { BadgeService } from '../services/badge.service';
import { RewardService } from '../services/reward.service';
import { of, throwError } from 'rxjs';
import { FormsModule } from '@angular/forms';
import { NO_ERRORS_SCHEMA } from '@angular/core';

describe('ListBadgeComponent', () => {
    let component: ListBadgeComponent;
    let fixture: ComponentFixture<ListBadgeComponent>;
    let badgeService: jasmine.SpyObj<BadgeService>;
    let rewardService: jasmine.SpyObj<RewardService>;

    beforeEach(async () => {
        const badgeSpy = jasmine.createSpyObj('BadgeService', ['list', 'update', 'deleteById', 'assignById']);
        const rewardSpy = jasmine.createSpyObj('RewardService', ['getHistory', 'assignPendingRewards', 'recalculateLevels']);

        await TestBed.configureTestingModule({
            declarations: [ListBadgeComponent],
            imports: [FormsModule],
            providers: [
                { provide: BadgeService, useValue: badgeSpy },
                { provide: RewardService, useValue: rewardSpy }
            ],
            schemas: [NO_ERRORS_SCHEMA]
        }).compileComponents();

        badgeService = TestBed.inject(BadgeService) as jasmine.SpyObj<BadgeService>;
        rewardService = TestBed.inject(RewardService) as jasmine.SpyObj<RewardService>;
    });

    beforeEach(() => {
        badgeService.list.and.returnValue(of([
            { id: 1, name: 'Superstar', isActive: true, autoAssignable: true },
            { id: 2, name: 'Beginner', isActive: false, autoAssignable: false }
        ]));
        rewardService.getHistory.and.returnValue(of([]));

        fixture = TestBed.createComponent(ListBadgeComponent);
        component = fixture.componentInstance;
        fixture.detectChanges();
    });

    it('should load badges on initialization', () => {
        expect(component.badges.length).toBe(2);
        expect(badgeService.list).toHaveBeenCalled();
    });

    it('should filter badges by search term', () => {
        component.searchTerm = 'super';
        fixture.detectChanges();
        expect(component.filteredBadges.length).toBe(1);
        expect(component.filteredBadges[0].name).toBe('Superstar');
    });

    it('should call assignById and show success feedback', () => {
        badgeService.assignById.and.returnValue(of({ assignedRewards: 5 }));
        const badge = component.badges[0];

        component.assignBadge(badge);

        expect(badgeService.assignById).toHaveBeenCalledWith(badge.id!);
        expect(component.feedbackType).toBe('success');
        expect(component.feedbackMessage).toContain('5 user(s) received the "Superstar" badge');
    });

    it('should handle errors during assignment', () => {
        badgeService.assignById.and.returnValue(throwError(() => new Error('Server Error')));
        const badge = component.badges[0];

        component.assignBadge(badge);

        expect(component.feedbackType).toBe('error');
        expect(component.feedbackMessage).toBe('Assignment action failed.');
        expect(component.isLoading).toBeFalse();
    });

    it('should toggle badge activity status', () => {
        const badge = component.badges[0];
        badgeService.update.and.returnValue(of(badge));

        component.toggleActive(badge);

        expect(badgeService.update).toHaveBeenCalled();
        expect(component.feedbackMessage).toContain('disabled');
    });
});
