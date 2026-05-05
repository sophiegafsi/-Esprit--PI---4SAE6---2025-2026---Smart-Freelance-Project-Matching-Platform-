import { Component, OnInit } from '@angular/core';
import { AuthService } from '../services/auth.service';

@Component({
    selector: 'app-profile-client',
    templateUrl: './profile-client.component.html',
    styleUrls: ['./profile-client.component.css']
})
export class ProfileClientComponent implements OnInit {
    isEditing = false;
    currentUser: any = null;
    editUser: any = {};

    clientView = {
        name: '',
        image: 'https://via.placeholder.com/200x200?text=Profile',
        email: '',
        country: '',
        birthDate: ''
    };

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
        this.clientView.name = `${user.firstName} ${user.lastName}`;
        this.clientView.email = user.email;
        this.clientView.country = user.country || 'Not specified';
        this.clientView.birthDate = user.birthDate || 'Not specified';
        this.clientView.image = user.imageUrl || 'https://via.placeholder.com/200x200?text=Profile';
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
        if (confirm('Êtes-vous sûr de vouloir supprimer votre compte client? Cette action est irréversible.')) {
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
                this.clientView.image = e.target.result;
            };
            reader.readAsDataURL(file);
        }
    }
}
