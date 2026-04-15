import { Component, OnInit } from '@angular/core';
import { TimeTrackingService } from '../services/time-tracking.service';
import { ActivatedRoute } from '@angular/router';

@Component({
  selector: 'app-work-review',
  templateUrl: './work-review.component.html',
  styleUrls: ['./work-review.component.css']
})
export class WorkReviewComponent implements OnInit {
  public contractId: string = '00000000-0000-0000-0000-000000000000'; // Default/mock for now
  public workSessions: any[] = [];
  public selectedSession: any = null;

  constructor(private timeTrackingService: TimeTrackingService, private route: ActivatedRoute) { }

  ngOnInit(): void {
    this.contractId = this.route.snapshot.paramMap.get('contractId') || this.contractId;
    this.loadSessions();
  }

  loadSessions() {
    this.timeTrackingService.getSessionsByContract(this.contractId).subscribe(res => {
      this.workSessions = res;
    });
  }

  viewSnapshots(session: any) {
    this.selectedSession = session;
  }

  closeSnapshots() {
    this.selectedSession = null;
  }

  approveSession(sessionId: string) {
    this.timeTrackingService.updateSessionStatus(sessionId, 'APPROVED').subscribe(() => {
      this.loadSessions();
      this.closeSnapshots();
    });
  }

  rejectSession(sessionId: string) {
    this.timeTrackingService.updateSessionStatus(sessionId, 'REJECTED').subscribe(() => {
      this.loadSessions();
      this.closeSnapshots();
    });
  }
}
