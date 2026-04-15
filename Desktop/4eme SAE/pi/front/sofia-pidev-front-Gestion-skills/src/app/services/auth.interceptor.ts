import { inject, PLATFORM_ID } from '@angular/core';
import { isPlatformBrowser } from '@angular/common';
import { Router } from '@angular/router';
import { catchError, throwError } from 'rxjs';
import { HttpInterceptorFn, HttpErrorResponse } from '@angular/common/http';

export const authInterceptor: HttpInterceptorFn = (req, next) => {
    const platformId = inject(PLATFORM_ID);
    // Must call inject() here (in injection context), NOT inside catchError callback
    const router = inject(Router);

    if (isPlatformBrowser(platformId)) {
        const token = localStorage.getItem('access_token');

        // Send anonymous requests for Notifications so Spring Security permitAll() natively accepts them 
        if (token && !req.url.includes('/notifications')) {
            const cloned = req.clone({
                setHeaders: {
                    Authorization: `Bearer ${token}`
                }
            });
            return next(cloned).pipe(
                catchError((error: HttpErrorResponse) => {
                    if (error.status === 401) {
                        // Prevent background polling from triggering a destructive logout
                        if (!req.url.includes('/notifications')) {
                            localStorage.removeItem('access_token');
                            router.navigate(['/login']);
                        }
                    }
                    return throwError(() => error);
                })
            );
        }
    }

    return next(req).pipe(
        catchError((error: HttpErrorResponse) => {
            if (error.status === 401) {
                if (!req.url.includes('/notifications')) {
                    if (typeof localStorage !== 'undefined') {
                        localStorage.removeItem('access_token');
                    }
                    router.navigate(['/login']);
                }
            }
            return throwError(() => error);
        })
    );
};

