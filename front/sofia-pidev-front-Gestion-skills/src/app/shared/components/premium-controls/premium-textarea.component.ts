import { Component, Input, forwardRef } from '@angular/core';
import { ControlValueAccessor, NG_VALUE_ACCESSOR } from '@angular/forms';

@Component({
  selector: 'app-premium-textarea',
  template: `
    <div class="premium-field" [class.has-focus]="isFocused" [class.has-value]="value" [class.has-error]="error">
      <label *ngIf="label" [for]="id">{{ label }}</label>
      <div class="input-wrapper glass">
        <textarea
          [id]="id"
          [placeholder]="placeholder"
          [disabled]="disabled"
          [rows]="rows"
          [(ngModel)]="value"
          (focus)="onFocus()"
          (blur)="onBlur()"
          (ngModelChange)="onChange($event)"
          class="premium-input-element"
        ></textarea>
        <div class="border-gradient"></div>
      </div>
      <div *ngIf="error" class="error-msg anim-fade-in">{{ error }}</div>
    </div>
  `,
  styles: [`
    .premium-field {
      margin-bottom: 1.5rem;
      position: relative;
    }
    label {
      display: block;
      font-size: 0.85rem;
      font-weight: 500;
      color: rgba(255, 255, 255, 0.6);
      margin-bottom: 0.5rem;
      transition: all 0.3s ease;
    }
    .has-focus label {
      color: var(--primary);
    }
    .input-wrapper {
      position: relative;
      border-radius: 12px;
      overflow: hidden;
      transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
    }
    .premium-input-element {
      width: 100%;
      background: transparent;
      border: none;
      padding: 12px 16px;
      color: white;
      font-family: inherit;
      font-size: 0.95rem;
      outline: none;
      resize: vertical;
      min-height: 100px;
    }
    .premium-input-element::placeholder {
      color: rgba(255, 255, 255, 0.2);
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
    .has-focus .border-gradient {
      width: 100%;
    }
    .has-focus .input-wrapper {
      background: rgba(255, 255, 255, 0.05);
      box-shadow: 0 0 20px rgba(243, 156, 18, 0.1);
    }
    .has-error .input-wrapper {
      border-color: rgba(231, 76, 60, 0.3);
      background: rgba(231, 76, 60, 0.05);
    }
    .has-error label {
      color: #ff6b6b;
    }
    .error-msg {
      color: #ff6b6b;
      font-size: 0.75rem;
      margin-top: 0.5rem;
      font-weight: 500;
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
      useExisting: forwardRef(() => PremiumTextareaComponent),
      multi: true
    }
  ]
})
export class PremiumTextareaComponent implements ControlValueAccessor {
  @Input() label: string = '';
  @Input() placeholder: string = '';
  @Input() rows: number = 4;
  @Input() id: string = 'textarea-' + Math.random().toString(36).substring(2, 9);
  @Input() error: string = '';

  value: any = '';
  isFocused = false;
  disabled = false;

  onChange: any = () => { };
  onTouched: any = () => { };

  onFocus() {
    this.isFocused = true;
    this.onTouched();
  }

  onBlur() {
    this.isFocused = false;
  }

  writeValue(value: any): void {
    this.value = value;
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
