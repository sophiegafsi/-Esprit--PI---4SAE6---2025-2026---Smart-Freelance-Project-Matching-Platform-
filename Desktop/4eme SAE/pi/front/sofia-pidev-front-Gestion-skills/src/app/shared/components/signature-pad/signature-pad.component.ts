import { Component, ElementRef, EventEmitter, Input, Output, ViewChild, AfterViewInit } from '@angular/core';

@Component({
  selector: 'app-signature-pad',
  template: `
    <div class="signature-wrapper">
      <div class="signature-header">
        <div class="header-left">
          <i class="fas fa-pen-nib me-2"></i>
          <span>{{ label }}</span>
        </div>
        <button type="button" class="btn-clear-premium" (click)="clear()" title="Clear signature">
          <i class="fas fa-eraser"></i> Clear
        </button>
      </div>
      
      <div class="canvas-container">
        <canvas #sigCanvas 
          (mousedown)="onMouseDown($event)" 
          (mousemove)="onMouseMove($event)" 
          (mouseup)="onMouseUp()"
          (touchstart)="onTouchStart($event)"
          (touchmove)="onTouchMove($event)"
          (touchend)="onTouchEnd()"
          class="signature-canvas">
        </canvas>
        <div class="canvas-overlay" *ngIf="!hasDrawn">
          <span class="placeholder-text">Sign Here</span>
        </div>
      </div>
      
      <div class="signature-footer">
        <i class="fas fa-shield-alt me-1"></i>
        <span>Secure Digital Signature</span>
      </div>
    </div>
  `,
  styles: [`
    .signature-wrapper {
      background: rgba(255, 255, 255, 0.03);
      border: 1px solid rgba(255, 255, 255, 0.1);
      border-radius: 12px;
      padding: 12px;
      backdrop-filter: blur(10px);
      box-shadow: inset 0 0 20px rgba(0,0,0,0.2);
    }
    .signature-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 10px;
    }
    .header-left {
      color: rgba(255,255,255,0.7);
      font-size: 0.85rem;
      font-weight: 500;
      letter-spacing: 0.5px;
    }
    .canvas-container {
      position: relative;
      background: #ffffff;
      border-radius: 8px;
      overflow: hidden;
      box-shadow: 0 4px 12px rgba(0,0,0,0.1);
    }
    .signature-canvas {
      width: 100%;
      height: 160px;
      display: block;
      cursor: crosshair;
      touch-action: none;
    }
    .canvas-overlay {
      position: absolute;
      top: 0;
      left: 0;
      width: 100%;
      height: 100%;
      display: flex;
      align-items: center;
      justify-content: center;
      pointer-events: none;
      opacity: 0.3;
    }
    .placeholder-text {
      color: #999;
      font-family: 'Outfit', sans-serif;
      font-size: 1.2rem;
      font-weight: 300;
      letter-spacing: 2px;
      text-transform: uppercase;
    }
    .btn-clear-premium {
      background: rgba(255,255,255,0.05);
      border: 1px solid rgba(255,255,255,0.1);
      color: var(--primary);
      padding: 4px 10px;
      border-radius: 6px;
      font-size: 0.75rem;
      cursor: pointer;
      transition: all 0.2s;
    }
    .btn-clear-premium:hover {
      background: rgba(255,255,255,0.1);
      transform: translateY(-1px);
    }
    .signature-footer {
      margin-top: 8px;
      font-size: 0.65rem;
      color: rgba(255,255,255,0.4);
      display: flex;
      align-items: center;
      justify-content: flex-end;
      text-transform: uppercase;
      letter-spacing: 1px;
    }
  `]
})
export class SignaturePadComponent implements AfterViewInit {
  @Input() label: string = 'Draw your signature';
  @Output() signatureChange = new EventEmitter<string>();

  @ViewChild('sigCanvas') sigCanvas!: ElementRef<HTMLCanvasElement>;
  private ctx!: CanvasRenderingContext2D;
  private isDrawing = false;
  public hasDrawn = false;

  ngAfterViewInit() {
    this.ctx = this.sigCanvas.nativeElement.getContext('2d')!;
    this.setupContext();
    this.resizeCanvas();
    window.addEventListener('resize', () => this.resizeCanvas());
  }

  private setupContext() {
    this.ctx.strokeStyle = '#000000';
    this.ctx.lineWidth = 2.5;
    this.ctx.lineCap = 'round';
    this.ctx.lineJoin = 'round';
  }

  private resizeCanvas() {
    const canvas = this.sigCanvas.nativeElement;
    const rect = canvas.getBoundingClientRect();
    const tempImage = canvas.toDataURL();
    canvas.width = rect.width;
    canvas.height = rect.height;
    this.setupContext();
    if (this.hasDrawn) {
      const img = new Image();
      img.onload = () => this.ctx.drawImage(img, 0, 0);
      img.src = tempImage;
    }
  }

  onMouseDown(e: MouseEvent) {
    this.isDrawing = true;
    this.hasDrawn = true;
    const rect = this.sigCanvas.nativeElement.getBoundingClientRect();
    this.ctx.beginPath();
    this.ctx.moveTo(e.clientX - rect.left, e.clientY - rect.top);
  }

  onMouseMove(e: MouseEvent) {
    if (!this.isDrawing) return;
    const rect = this.sigCanvas.nativeElement.getBoundingClientRect();
    this.ctx.lineTo(e.clientX - rect.left, e.clientY - rect.top);
    this.ctx.stroke();
  }

  onMouseUp() {
    if (this.isDrawing) {
      this.isDrawing = false;
      this.emitSignature();
    }
  }

  onTouchStart(e: TouchEvent) {
    e.preventDefault();
    if (e.touches.length > 0) {
      this.isDrawing = true;
      this.hasDrawn = true;
      const rect = this.sigCanvas.nativeElement.getBoundingClientRect();
      const touch = e.touches[0];
      this.ctx.beginPath();
      this.ctx.moveTo(touch.clientX - rect.left, touch.clientY - rect.top);
    }
  }

  onTouchMove(e: TouchEvent) {
    e.preventDefault();
    if (!this.isDrawing || e.touches.length === 0) return;
    const rect = this.sigCanvas.nativeElement.getBoundingClientRect();
    const touch = e.touches[0];
    this.ctx.lineTo(touch.clientX - rect.left, touch.clientY - rect.top);
    this.ctx.stroke();
  }

  onTouchEnd() {
    if (this.isDrawing) {
      this.isDrawing = false;
      this.emitSignature();
    }
  }

  clear() {
    const canvas = this.sigCanvas.nativeElement;
    this.ctx.clearRect(0, 0, canvas.width, canvas.height);
    this.hasDrawn = false;
    this.signatureChange.emit('');
  }

  private emitSignature() {
    const canvas = this.sigCanvas.nativeElement;
    this.signatureChange.emit(canvas.toDataURL());
  }
}
