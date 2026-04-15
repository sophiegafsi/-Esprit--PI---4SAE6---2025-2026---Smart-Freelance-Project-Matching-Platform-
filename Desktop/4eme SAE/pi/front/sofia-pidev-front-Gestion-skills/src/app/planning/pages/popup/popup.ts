import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { PopupService } from '../../services/popup.service';

@Component({
  selector: 'app-popup',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './popup.html',
  styleUrl: './popup.css'
})
export class PopupComponent {
  constructor(public popupService: PopupService) {}

  get popup() {
    return this.popupService.popup;
  }

  close(): void {
    this.popupService.close();
  }

  confirm(): void {
    this.popupService.handleConfirm();
  }

  cancel(): void {
    this.popupService.handleCancel();
  }
}