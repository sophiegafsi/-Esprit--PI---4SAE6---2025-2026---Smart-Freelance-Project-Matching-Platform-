import { Component, EventEmitter, Input, Output } from '@angular/core';

@Component({
    selector: 'app-contract-signature-modal',
    template: `
    <div class="modal-overlay" *ngIf="show">
      <div class="signature-modal-card glass">
        <!-- Header -->
        <div class="modal-header-premium mb-4">
          <div class="header-icon-seal">
            <i class="fas fa-file-signature"></i>
          </div>
          <div>
            <h3 class="m-0 text-white">Digital Agreement</h3>
            <p class="m-0 text-muted small">Electronic Signature Confirmation</p>
          </div>
          <button (click)="close()" class="btn-close-custom">
            <i class="fas fa-times"></i>
          </button>
        </div>

        <!-- Body -->
        <div class="modal-body-custom">
          <div class="agreement-notice mb-4">
            <div class="notice-icon">
              <i class="fas fa-info-circle"></i>
            </div>
            <p class="m-0">By providing your signature below, you legally agree to the terms and conditions set forth in the contract. This action is equivalent to a physical handwritten signature.</p>
          </div>

          <app-signature-pad 
            [label]="'Freelancer Signature'" 
            (signatureChange)="onSignatureChange($event)">
          </app-signature-pad>
        </div>

        <!-- Footer -->
        <div class="modal-footer-premium mt-4">
          <button (click)="close()" class="btn-premium ghost">Cancel</button>
          <button (click)="confirm()" class="btn-premium primary" [disabled]="!signatureData || isSubmitting">
            <i class="fas fa-check-circle me-2" *ngIf="!isSubmitting"></i>
            <i class="fas fa-spinner fa-spin me-2" *ngIf="isSubmitting"></i>
            {{ isSubmitting ? 'Processing...' : 'Confirm & Sign' }}
          </button>
        </div>
      </div>
    </div>
  `,
    styles: [`
    .modal-overlay {
      position: fixed;
      top: 0;
      left: 0;
      width: 100vw;
      height: 100vh;
      background: rgba(0, 0, 0, 0.8);
      backdrop-filter: blur(8px);
      display: flex;
      align-items: center;
      justify-content: center;
      z-index: 9999;
    }

    .signature-modal-card {
      width: 100%;
      max-width: 500px;
      padding: 2.5rem;
      border-radius: 20px;
      border: 1px solid rgba(255, 255, 255, 0.1);
      background: linear-gradient(145deg, rgba(20, 20, 25, 0.95), rgba(30, 30, 40, 0.95));
      box-shadow: 0 25px 50px -12px rgba(0, 0, 0, 0.5);
    }

    .header-icon-seal {
      width: 45px;
      height: 45px;
      background: linear-gradient(135deg, var(--primary), #4a90e2);
      border-radius: 12px;
      display: flex;
      align-items: center;
      justify-content: center;
      margin-right: 1rem;
      color: white;
      font-size: 1.25rem;
      box-shadow: 0 8px 16px rgba(0, 0, 0, 0.2);
    }

    .agreement-notice {
      display: flex;
      gap: 1rem;
      background: rgba(255, 255, 255, 0.03);
      padding: 1rem;
      border-radius: 12px;
      border-left: 3px solid var(--primary);
      color: rgba(255, 255, 255, 0.7);
      font-size: 0.85rem;
      line-height: 1.5;
    }

    .notice-icon {
      color: var(--primary);
      font-size: 1.1rem;
    }

    .modal-footer-premium {
      display: flex;
      justify-content: flex-end;
      gap: 1rem;
    }

    .btn-premium {
      padding: 0.8rem 1.5rem;
      border-radius: 10px;
      font-weight: 600;
      cursor: pointer;
      transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
      display: flex;
      align-items: center;
    }

    .btn-premium.primary {
      background: var(--primary);
      border: none;
      color: white;
    }

    .btn-premium.primary:hover:not(:disabled) {
      background: #4a90e2;
      transform: translateY(-2px);
      box-shadow: 0 10px 20px rgba(0, 0, 0, 0.2);
    }

    .btn-premium.ghost {
      background: transparent;
      border: 1px solid rgba(255, 255, 255, 0.1);
      color: rgba(255, 255, 255, 0.7);
    }

    .btn-premium.ghost:hover {
      background: rgba(255, 255, 255, 0.05);
      color: white;
    }

    .btn-premium:disabled {
      opacity: 0.5;
      cursor: not-allowed;
    }
  `]
})
export class ContractSignatureModalComponent {
    @Input() show: boolean = false;
    @Input() isSubmitting: boolean = false;
    @Output() onConfirm = new EventEmitter<string>();
    @Output() onClose = new EventEmitter<void>();

    signatureData: string = '';

    onSignatureChange(data: string) {
        this.signatureData = data;
    }

    confirm() {
        if (this.signatureData) {
            this.onConfirm.emit(this.signatureData);
        }
    }

    close() {
        this.onClose.emit();
    }
}
