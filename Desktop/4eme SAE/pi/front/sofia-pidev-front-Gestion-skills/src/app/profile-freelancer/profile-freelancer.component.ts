import { Component, OnInit } from '@angular/core';
import { AuthService } from '../services/auth.service';

interface Skill {
  name: string;
}

interface Project {
  title: string;
  description: string;
  image: string;
}

@Component({
  selector: 'app-profile-freelancer',
  templateUrl: './profile-freelancer.component.html',
  styleUrls: ['./profile-freelancer.component.css']
})
export class ProfileFreelancerComponent implements OnInit {
  isEditing = false;
  currentUser: any = null;
  editUser: any = {};

  freelancerView = {
    name: '',
    title: '',
    image: 'https://via.placeholder.com/200x200?text=Profile',
    about: '',
    rate: 0,
    rateUnit: '/h',
    rating: 5,
    ratingCount: 0,
    ratingText: 'No reviews yet.'
  };

  skills: Skill[] = [];

  projects: Project[] = [
    {
      title: 'Sample Project',
      description: 'Project details will appear here.',
      image: 'https://via.placeholder.com/300x200?text=Project'
    }
  ];

  constructor(private authService: AuthService) { }

  ngOnInit(): void {
    this.authService.currentUser$.subscribe(user => {
      if (user) {
        this.currentUser = user;
        this.updateView(user);
      }
    });
  }

  updateView(user: any): void {
    this.freelancerView.name = `${user.firstName} ${user.lastName}`;
    this.freelancerView.title = user.jobTitle || 'Freelancer';
    this.freelancerView.about = user.bio || 'No bio provided.';
    this.freelancerView.rate = user.hourlyRate || 0;
    this.freelancerView.image = user.imageUrl || 'https://via.placeholder.com/200x200?text=Profile';

    if (user.skills) {
      this.skills = user.skills.split(',').map((s: string) => ({ name: s.trim() }));
    } else {
      this.skills = [];
    }
  }

  toggleEdit(): void {
    if (!this.isEditing) {
      this.editUser = { ...this.currentUser };
    }
    this.isEditing = !this.isEditing;
  }

  saveProfile(): void {
    this.authService.updateUser(this.editUser).subscribe({
      next: (updatedUser) => {
        this.isEditing = false;
        this.updateView(updatedUser);
      },
      error: (err) => {
        console.error('Error updating profile:', err);
        alert('Failed to update profile. Please try again.');
      }
    });
  }

  cancelEdit(): void {
    this.isEditing = false;
  }

  deleteProfile(): void {
    if (confirm('Êtes-vous sûr de vouloir supprimer votre profil ? Cette action est irréversible.')) {
      this.authService.deleteAccount(this.currentUser.id).subscribe({
        next: () => {
          this.authService.logout();
          window.location.href = '/login';
        },
        error: (err) => {
          console.error('Error deleting profile:', err);
          alert('Failed to delete profile. Please try again.');
        }
      });
    }
  }

  onFileSelected(event: any): void {
    const file = event.target.files[0];
    if (file) {
      const reader = new FileReader();
      reader.onload = (e: any) => {
        this.editUser.imageUrl = e.target.result;
        // Also update view immediately for preview
        this.freelancerView.image = e.target.result;
      };
      reader.readAsDataURL(file);
    }
  }

  contactFreelancer(): void {
    console.log('Contact freelancer:', this.freelancerView.name);
  }

  postJob(): void {
    console.log('Post a job');
  }
}
