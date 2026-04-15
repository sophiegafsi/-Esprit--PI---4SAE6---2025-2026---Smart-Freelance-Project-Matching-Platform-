import { Component, OnDestroy, OnInit } from '@angular/core';
import { TimeTrackingService } from '../services/time-tracking.service';
import { ActivatedRoute } from '@angular/router';

@Component({
  selector: 'app-time-tracker',
  templateUrl: './time-tracker.component.html',
  styleUrls: ['./time-tracker.component.css']
})
export class TimeTrackerComponent implements OnInit, OnDestroy {
  public contractId: string = '00000000-0000-0000-0000-000000000000'; // Default/mock for now
  public freelancerId: string = '00000000-0000-0000-0000-000000000000'; // Default/mock for now
  public currentSessionId: string | null = null;
  public isVisible: boolean = false;

  public isTracking: boolean = false;
  public timeElapsed: number = 0; // in seconds
  private interval: any;
  private snapshotInterval: any;

  constructor(private timeTrackingService: TimeTrackingService) { }

  ngOnInit(): void {
    this.timeTrackingService.showTracker$.subscribe(visible => {
      this.isVisible = visible;
      if (visible) {
        this.contractId = this.timeTrackingService.activeContractId || this.contractId;
        this.freelancerId = this.timeTrackingService.activeFreelancerId || this.freelancerId;
      }
    });
  }

  get formattedTime(): string {
    const hours = Math.floor(this.timeElapsed / 3600);
    const minutes = Math.floor((this.timeElapsed % 3600) / 60);
    const seconds = this.timeElapsed % 60;
    return `${this.pad(hours)}:${this.pad(minutes)}:${this.pad(seconds)}`;
  }

  private pad(val: number): string {
    return val < 10 ? '0' + val : val.toString();
  }

  startTimer() {
    this.timeTrackingService.startSession(this.contractId, this.freelancerId).subscribe(res => {
      this.currentSessionId = res.id;
      this.isTracking = true;
      this.timeElapsed = 0;

      this.interval = setInterval(() => {
        this.timeElapsed++;
      }, 1000);

      // Take an immediate snapshot for testing so you don't have to wait 60s
      this.takeSnapshot();

      // Take a mock snapshot every min for demonstration (usually ~10 mins)
      this.snapshotInterval = setInterval(() => {
        this.takeSnapshot();
      }, 60000);
    });
  }

  stopTimer() {
    if (this.currentSessionId) {
      this.timeTrackingService.stopSession(this.currentSessionId).subscribe(res => {
        // Take a final snapshot when stopping
        this.takeSnapshot();
        this.isTracking = false;
        clearInterval(this.interval);
        clearInterval(this.snapshotInterval);
        this.currentSessionId = null;
      });
    }
  }

  takeSnapshot() {
    if (!this.currentSessionId) return;
    const mockScreenshot = 'https://placehold.co/600x400.png?text=Mock+Screenshot';
    this.timeTrackingService.addSnapshot(this.currentSessionId, mockScreenshot).subscribe();
  }

  ngOnDestroy(): void {
    if (this.isTracking) {
      this.stopTimer();
    }
  }

  closeTracker() {
    if (this.isTracking) {
      alert("Freelink Rules: Please stop the active timer before closing the tracker window.");
      return;
    }
    this.timeTrackingService.closeTracker();
  }
}
