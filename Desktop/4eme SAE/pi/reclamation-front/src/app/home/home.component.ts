// src/app/home/home.component.ts
import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';

@Component({
  selector: 'app-home',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './home.component.html',
  styleUrls: ['./home.component.css']
})
export class HomeComponent {
  // Hero statistics
  heroStats = [
    { value: '1,200', label: 'Freelancers' },
    { value: '350', label: 'Active Jobs' },
    { value: '24/7', label: 'Support' }
  ];

  // How it works steps
  steps = [
    { icon: '📌', title: '1. Post a job', description: 'The client describes the need' },
    { icon: '📝', title: '2. Receive applications', description: 'Freelancers apply' },
    { icon: '🤝', title: '3. Choose & collaborate', description: 'Start the project together' }
  ];

  // Job offers grid
  gridOffers = [
    {
      title: 'UI/UX Designer',
      description: 'Landing page + Figma mockups • 2 weeks',
      tags: ['Figma', 'UX', 'Wireframes'],
      tag: 'Remote',
      salary: '$300–$500'
    },
    {
      title: 'Backend Developer (Spring)',
      description: 'REST API + security • 1 month',
      tags: ['Spring', 'JWT', 'PostgreSQL'],
      tag: 'Hybrid',
      salary: '$600–$900'
    },
    {
      title: 'Web Developer (Symfony)',
      description: 'E-commerce project • 1 month',
      tags: ['Symfony', 'MySQL', 'API'],
      tag: 'Remote',
      salary: '$500–$800'
    }
  ];

  // Job options for dropdown
  jobOptions = [
    'Web Developer (Symfony)',
    'Backend Developer (Spring)',
    'UI/UX Designer'
  ];
}
