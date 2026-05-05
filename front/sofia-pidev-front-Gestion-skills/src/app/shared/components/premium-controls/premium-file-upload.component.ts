import { Component, Input, Output, EventEmitter, forwardRef, ViewChild, ElementRef } from '@angular/core';
import { ControlValueAccessor, NG_VALUE_ACCESSOR } from '@angular/forms';

@Component({
    selector: 'app-premium-file-upload',
    template: `
    <div class="premium-field" [class.has-file]="!!fileName">
      <label *ngIf="label">{{ label }}</label>
      <div class="upload-wrapper glass" (click)="fileInput.click()" [class.dragging]="isDragging" 
           (dragover)="onDragOver($event)" (dragleave)="onDragLeave($event)" (drop)="onDrop($event)">
        
        <input #fileInput type="file" [accept]="accept" (change)="onFileSelected($event)" style="display: none;">
        
        <div class="upload-content" *ngIf="!fileName">
          <i class="fas fa-cloud-upload-alt upload-icon"></i>
          <span class="upload-text">{{ placeholder }}</span>
        </div>

        <div class="file-content" *ngIf="fileName">
          <i class="fas fa-file-pdf file-icon" *ngIf="isPdf"></i>
          <i class="fas fa-file-image file-icon" *ngIf="isImage"></i>
          <i class="fas fa-file-alt file-icon" *ngIf="!isPdf && !isImage"></i>
          <div class="file-info">
            <span class="file-name">{{ fileName }}</span>
            <span class="file-size" *ngIf="fileSize">{{ fileSize }}</span>
          </div>
          <button type="button" class="clear-btn" (click)="clearFile($event)">
            <i class="fas fa-times"></i>
          </button>
        </div>

        <div class="border-gradient"></div>
      </div>
      <div *ngIf="error" class="error-msg anim-fade-in">{{ error }}</div>
    </div>
  `,
    styles: [`
    .premium-field {
      margin-bottom: 1.5rem;
    }
    label {
      display: block;
      font-size: 0.85rem;
      font-weight: 500;
      color: rgba(255, 255, 255, 0.6);
      margin-bottom: 0.5rem;
    }
    .upload-wrapper {
      position: relative;
      min-height: 80px;
      display: flex;
      align-items: center;
      justify-content: center;
      border-radius: 12px;
      cursor: pointer;
      transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
      padding: 1rem;
      border: 1px dashed rgba(255, 255, 255, 0.1);
    }
    .upload-wrapper.dragging {
      background: rgba(243, 156, 18, 0.1);
      border-color: var(--primary);
      transform: scale(1.02);
    }
    .upload-content {
      display: flex;
      flex-direction: column;
      align-items: center;
      gap: 0.5rem;
    }
    .upload-icon {
      font-size: 1.5rem;
      color: var(--primary);
      opacity: 0.7;
    }
    .upload-text {
      font-size: 0.9rem;
      color: rgba(255, 255, 255, 0.4);
    }
    .file-content {
      display: flex;
      align-items: center;
      width: 100%;
      gap: 1rem;
    }
    .file-icon {
      font-size: 2rem;
      color: var(--primary);
    }
    .file-info {
      flex: 1;
      display: flex;
      flex-direction: column;
      overflow: hidden;
    }
    .file-name {
      color: white;
      font-size: 0.95rem;
      white-space: nowrap;
      overflow: hidden;
      text-overflow: ellipsis;
    }
    .file-size {
      font-size: 0.75rem;
      color: rgba(255, 255, 255, 0.4);
    }
    .clear-btn {
      background: rgba(255, 255, 255, 0.05);
      border: none;
      color: white;
      width: 28px;
      height: 28px;
      border-radius: 50%;
      display: flex;
      align-items: center;
      justify-content: center;
      cursor: pointer;
      transition: all 0.2s;
    }
    .clear-btn:hover {
      background: rgba(231, 76, 60, 0.3);
      color: #ff6b6b;
    }
    .error-msg {
      color: #ff6b6b;
      font-size: 0.75rem;
      margin-top: 0.5rem;
      font-weight: 500;
    }
    .border-gradient {
      position: absolute;
      bottom: 0;
      left: 0;
      width: 0;
      height: 2px;
      background: linear-gradient(90deg, var(--primary), #4a90e2);
      transition: width 0.3s ease;
    }
    .has-file .border-gradient {
      width: 100%;
    }
    @keyframes fadeIn {
      from { opacity: 0; transform: translateY(-5px); }
      to { opacity: 1; transform: translateY(0); }
    }
    .anim-fade-in {
      animation: fadeIn 0.3s ease forwards;
    }
  `],
    providers: [
        {
            provide: NG_VALUE_ACCESSOR,
            useExisting: forwardRef(() => PremiumFileUploadComponent),
            multi: true
        }
    ]
})
export class PremiumFileUploadComponent implements ControlValueAccessor {
    @Input() label: string = '';
    @Input() placeholder: string = 'Click or drag file to upload';
    @Input() accept: string = '*/*';
    @Input() error: string = '';

    @Output() fileChange = new EventEmitter<File | undefined>();

    fileName: string = '';
    fileSize: string = '';
    isDragging = false;
    disabled = false;

    isPdf = false;
    isImage = false;

    onChange: any = () => { };
    onTouched: any = () => { };

    onFileSelected(event: any) {
        const file = event.target.files[0];
        this.handleFile(file);
    }

    onDragOver(event: DragEvent) {
        event.preventDefault();
        event.stopPropagation();
        this.isDragging = true;
    }

    onDragLeave(event: DragEvent) {
        event.preventDefault();
        event.stopPropagation();
        this.isDragging = false;
    }

    onDrop(event: DragEvent) {
        event.preventDefault();
        event.stopPropagation();
        this.isDragging = false;
        const file = event.dataTransfer?.files[0];
        if (file) {
            this.handleFile(file);
        }
    }

    handleFile(file: File | undefined) {
        if (file) {
            this.fileName = file.name;
            this.fileSize = this.formatBytes(file.size);
            this.isPdf = file.type === 'application/pdf';
            this.isImage = file.type.startsWith('image/');
            this.onChange(file);
            this.fileChange.emit(file);
        } else {
            this.clearFile();
        }
        this.onTouched();
    }

    clearFile(event?: Event) {
        if (event) {
            event.stopPropagation();
        }
        this.fileName = '';
        this.fileSize = '';
        this.isPdf = false;
        this.isImage = false;
        this.onChange(undefined);
        this.fileChange.emit(undefined);
    }

    formatBytes(bytes: number, decimals = 2) {
        if (bytes === 0) return '0 Bytes';
        const k = 1024;
        const dm = decimals < 0 ? 0 : decimals;
        const sizes = ['Bytes', 'KB', 'MB', 'GB'];
        const i = Math.floor(Math.log(bytes) / Math.log(k));
        return parseFloat((bytes / Math.pow(k, i)).toFixed(dm)) + ' ' + sizes[i];
    }

    writeValue(value: any): void {
        if (!value) {
            this.fileName = '';
            this.fileSize = '';
        }
    }

    registerOnChange(fn: any): void {
        this.onChange = fn;
    }

    registerOnTouched(fn: any): void {
        this.onTouched = fn;
    }

    setDisabledState(isDisabled: boolean): void {
        this.disabled = isDisabled;
    }
}
