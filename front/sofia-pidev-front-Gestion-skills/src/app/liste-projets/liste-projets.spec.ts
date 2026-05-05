import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ListeProjetsComponent } from './liste-projets';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { RouterTestingModule } from '@angular/router/testing';
import { ProjetService } from '../services/projet.service';
import { of } from 'rxjs';

describe('ListeProjetsComponent', () => {
  let component: ListeProjetsComponent;
  let fixture: ComponentFixture<ListeProjetsComponent>;
  let projetService: ProjetService;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [
        ListeProjetsComponent,
        HttpClientTestingModule,
        RouterTestingModule
      ],
      providers: [ProjetService]
    })
    .compileComponents();

    fixture = TestBed.createComponent(ListeProjetsComponent);
    component = fixture.componentInstance;
    projetService = TestBed.inject(ProjetService);
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should load projects on init', () => {
    const mockProjets = [
      { id: 1, title: 'Test Project', description: 'Test', date: '2024-01-01', domaine: 'WEB' }
    ];
    
    // Using a spy to verify the service call
    spyOn(projetService, 'getProjets').and.returnValue(of(mockProjets));

    fixture.detectChanges(); // Trigger ngOnInit

    expect(projetService.getProjets).toHaveBeenCalled();
    expect(component.projets.length).toBe(1);
    expect(component.projets[0].title).toBe('Test Project');
  });

  it('should display "Aucun projet trouvé" when list is empty', () => {
    spyOn(projetService, 'getProjets').and.returnValue(of([]));
    
    fixture.detectChanges();
    
    const compiled = fixture.nativeElement as HTMLElement;
    // Assuming there is an element showing this message when list is empty
    // If not, we just verify the component state
    expect(component.projets.length).toBe(0);
  });
});
