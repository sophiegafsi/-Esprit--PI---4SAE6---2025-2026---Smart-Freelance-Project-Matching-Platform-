import { Component } from '@angular/core';
import { AuthService } from '../services/auth.service';
import { Router } from '@angular/router';

@Component({
  selector: 'app-accueil',
  templateUrl: './accueil.component.html',
  styleUrls: ['./accueil.component.css']
})
export class AccueilComponent {
  isLoggedIn = false;
  user: any = null;

  constructor(private authService: AuthService, private router: Router) { }

  ngOnInit() {
    this.authService.getCurrentUser().subscribe({
      next: (data) => {
        this.isLoggedIn = true;
        this.user = data;
      },
      error: () => {
        this.isLoggedIn = false;
      }
    });
  }

  login() {
    this.router.navigate(['/login']);
  }

  logout() {
    this.authService.logout();
    this.isLoggedIn = false;
    this.user = null;
  }
}
