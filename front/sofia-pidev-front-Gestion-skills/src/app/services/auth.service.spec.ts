import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { AuthService } from './auth.service';
import { EMPTY, of } from 'rxjs';

describe('AuthService', () => {
  let service: AuthService;
  let httpMock: HttpTestingController;

  const KEYCLOAK_URL =
    'http://localhost:8080/realms/freelink-realm/protocol/openid-connect/token';
  const USER_API_URL = 'http://localhost:8081/user/api/users';

  beforeEach(() => {
    // Clear localStorage so isLoggedIn() returns false by default
    localStorage.clear();

    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [AuthService],
    });

    service = TestBed.inject(AuthService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify(); // Ensure no outstanding HTTP requests
    localStorage.clear();
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('isLoggedIn() should return false when no access_token in localStorage', () => {
    expect(service.isLoggedIn()).toBeFalse();
  });

  it('isLoggedIn() should return true after saveToken() is called', () => {
    // Spy on getCurrentUser so it does not fire a real HTTP call
    spyOn(service, 'getCurrentUser').and.returnValue(EMPTY);

    service.saveToken({ access_token: 'fake-jwt-token', refresh_token: 'fake-refresh' });

    expect(service.isLoggedIn()).toBeTrue();
    expect(localStorage.getItem('access_token')).toBe('fake-jwt-token');
  });

  it('logout() should clear tokens and update authState to false', () => {
    localStorage.setItem('access_token', 'some-token');
    localStorage.setItem('refresh_token', 'some-refresh');

    let authStateValue: boolean | undefined;
    service.authState$.subscribe((val) => (authStateValue = val));

    service.logout();

    expect(localStorage.getItem('access_token')).toBeNull();
    expect(localStorage.getItem('refresh_token')).toBeNull();
    expect(authStateValue).toBeFalse();
  });

  it('isFreelancer() should return true when user role includes "freelancer"', () => {
    // Manually push a user into the internal subject
    (service as any).currentUserSubject.next({ role: 'freelancer,client' });
    expect(service.isFreelancer()).toBeTrue();
  });

  it('isAdmin() should return false when user role does not include "admin"', () => {
    (service as any).currentUserSubject.next({ role: 'client' });
    expect(service.isAdmin()).toBeFalse();
  });

  it('login() should POST to Keycloak token endpoint with correct body', () => {
    service.login('user@test.com', 'secret').subscribe();

    const req = httpMock.expectOne(KEYCLOAK_URL);
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toContain('username=user%40test.com');
    expect(req.request.body).toContain('grant_type=password');

    req.flush({ access_token: 'jwt', refresh_token: 'refresh' });
  });

  it('getCurrentUser() should GET /me and emit the user via currentUser$', () => {
    const mockUser = { id: '123', name: 'Jane Doe', role: 'freelancer' };

    let emittedUser: any;
    service.currentUser$.subscribe((u) => (emittedUser = u));

    // The implementation of getCurrentUser() in auth.service.ts calls .subscribe() internally
    // and also returns the observable. This triggers TWO requests for the same URL.
    service.getCurrentUser().subscribe();

    const reqs = httpMock.match(`${USER_API_URL}/me`);
    expect(reqs.length).toBe(2);
    reqs.forEach(req => {
        expect(req.request.method).toBe('GET');
        req.flush(mockUser);
    });

    expect(emittedUser).toEqual(mockUser);
  });
});
