import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';

type Lesson = {
  id: number;
  title: string;
  description?: string;
  orderIndex: number;
};

type Formation = {
  id: number;
  titre: string;
  description: string;
  status: 'DRAFT' | 'PUBLISHED' | 'ARCHIVED';
  createdAt: string;
  lessons: Lesson[];
};

@Component({
  selector: 'app-formation-content',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './formation-content.html',
  styleUrls: ['./formation-content.css'],
})
export class FormationContentComponent {
  formation?: Formation;

  private formations: Formation[] = [
    {
      id: 1,
      titre: 'Symfony for Beginners',
      description: 'Learn Symfony basics: routing, controllers, Twig, forms, validation, and CRUD.',
      status: 'PUBLISHED',
      createdAt: '2026-02-15T10:00:00',
      lessons: [
        { id: 101, title: 'Introduction & Setup', description: 'Install Symfony and understand structure.', orderIndex: 1 },
        { id: 102, title: 'Routing & Controllers', description: 'Create routes and controllers.', orderIndex: 2 },
        { id: 103, title: 'Twig Templates', description: 'Build UI with Twig.', orderIndex: 3 },
      ],
    },
    {
      id: 2,
      titre: 'CI/CD with Jenkins',
      description: 'Pipelines, builds, tests, SonarQube, Docker, deployment.',
      status: 'DRAFT',
      createdAt: '2026-02-10T10:00:00',
      lessons: [
        { id: 201, title: 'Jenkins Basics', description: 'Jobs, pipelines, agents.', orderIndex: 1 },
        { id: 202, title: 'Pipeline as Code', description: 'Jenkinsfile and stages.', orderIndex: 2 },
      ],
    },
  ];

  constructor(private route: ActivatedRoute, private router: Router) {
    const id = Number(this.route.snapshot.paramMap.get('id'));
    this.formation = this.formations.find((f) => f.id === id);
  }

  back() {
    this.router.navigate(['/trainings']);
  }

  viewLesson(lessonId: number) {
    alert('View Lesson: ' + lessonId);
  }

  startLessonQuiz(lessonId: number) {
    alert('Start Quiz for Lesson: ' + lessonId);
  }

  startFinalQuiz() {
    alert('Start Final Quiz');
  }
}
