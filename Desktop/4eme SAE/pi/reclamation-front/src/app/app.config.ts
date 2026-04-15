// src/app/app.config.ts
import { ApplicationConfig,  provideBrowserGlobalErrorListeners} from '@angular/core';
import { provideRouter ,withRouterConfig} from '@angular/router';
import { provideHttpClient } from '@angular/common/http';  // 🔥 AJOUTER
import { routes } from './app.routes';


export const appConfig: ApplicationConfig = {
  providers: [
    provideBrowserGlobalErrorListeners(),
    provideRouter(routes ,withRouterConfig({ onSameUrlNavigation: 'reload' })),
    provideHttpClient(),   // 🔥 TRÈS IMPORTANT

  ]
};
