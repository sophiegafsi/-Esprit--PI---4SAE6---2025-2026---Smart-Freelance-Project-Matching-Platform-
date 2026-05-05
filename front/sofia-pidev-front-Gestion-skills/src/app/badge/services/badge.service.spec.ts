import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { BadgeService } from './badge.service';
import { Badge } from '../models/badge.model';

describe('BadgeService', () => {
    let service: BadgeService;
    let httpMock: HttpTestingController;

    beforeEach(() => {
        TestBed.configureTestingModule({
            imports: [HttpClientTestingModule],
            providers: [BadgeService]
        });
        service = TestBed.inject(BadgeService);
        httpMock = TestBed.inject(HttpTestingController);
    });

    afterEach(() => {
        httpMock.verify();
    });

    it('should be created', () => {
        expect(service).toBeTruthy();
    });

    it('should retrieve all badges via GET', () => {
        const dummyBadges: Partial<Badge>[] = [
            { id: 1, name: 'Gold Performer', isActive: true },
            { id: 2, name: 'Silver Star', isActive: true }
        ];

        service.list().subscribe(badges => {
            expect(badges.length).toBe(2);
            expect(badges[0].name).toBe('Gold Performer');
        });

        const req = httpMock.expectOne(request =>
            request.url.endsWith('/api/badges') && request.method === 'GET'
        );
        req.flush(dummyBadges);
    });

    it('should trigger manual badge assignment via POST', () => {
        const mockResponse = { message: 'Success', assignedRewards: 3 };
        const badgeId = 101;

        service.assignById(badgeId).subscribe(res => {
            expect(res.assignedRewards).toBe(3);
            expect(res.message).toBe('Success');
        });

        const req = httpMock.expectOne(request =>
            request.url.endsWith(`/api/badges/${badgeId}/assign`) && request.method === 'POST'
        );
        req.flush(mockResponse);
    });

    it('should handle error when creation fails', () => {
        const newBadge = { name: 'Fail Badge' };

        service.create(newBadge as any).subscribe({
            next: () => fail('Should have failed'),
            error: (error) => {
                expect(error.status).toBe(500);
            }
        });

        const req = httpMock.expectOne(request => request.method === 'POST');
        req.flush('Server Error', { status: 500, statusText: 'Internal Server Error' });
    });
});
