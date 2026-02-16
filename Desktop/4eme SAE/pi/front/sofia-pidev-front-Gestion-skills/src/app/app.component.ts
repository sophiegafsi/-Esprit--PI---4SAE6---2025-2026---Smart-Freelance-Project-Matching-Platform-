import { Component, OnInit } from '@angular/core';
import { AuthService } from './services/auth.service';

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrl: './app.component.css'
})
export class AppComponent implements OnInit {
  title = 'gestion-skills';

  constructor(private authService: AuthService) { }

  ngOnInit(): void {
    // Automatically load user data if logged in
    // This ensures syncUser() is called on the backend
    if (this.authService.isLoggedIn()) {
      this.authService.loadCurrentUser();
    }
  }
}
