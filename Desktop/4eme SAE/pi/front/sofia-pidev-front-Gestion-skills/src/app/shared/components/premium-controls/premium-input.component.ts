import { Component, Input, forwardRef } from '@angular/core';
import { ControlValueAccessor, NG_VALUE_ACCESSOR } from '@angular/forms';

@Component({
    selector: 'app-premium-input',
    template: `
    <div class="premium-field" [class.has-focus]="isFocused" [class.has-value]="value" [class.has-error]="error">
      <label *ngIf="label" [for]="id">{{ label }}</label>
      <div class="input-wrapper glass">
        <i *ngIf="icon" [class]="'fas ' + icon + ' input-icon'"></i>
        <input
          [id]="id"
          [type]="type"
          [placeholder]="placeholder"
          [disabled]="disabled"
          [(ngModel)]="value"
          (focus)="onFocus()"
          (blur)="onBlur()"
          (input)="onChange(value)"
          class="premium-input-element"
        />
        <div class="border-gradient"></div>
      </div>
      <div *ngIf="error" class="error-msg anim-fade-in">{{ error }}</div>
    </div>
  `,
    styles: [`
    .premium-field {
      margin-bottom: 1.5rem;
      position: relative;
      transition: all 0.3s ease;
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
      transform: translateY(-2px);
    }
    .input-wrapper {
      position: relative;
      display: flex;
      align-items: center;
      border-radius: 12px;
      overflow: hidden;
      transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
    }
    .input-icon {
      padding-left: 1rem;
      color: rgba(255, 255, 255, 0.3);
      font-size: 1rem;
      transition: all 0.3s ease;
    }
    .has-focus .input-icon {
      color: var(--primary);
    }
    .premium-input-element {
      width: 100%;
      background: transparent;
      border: none;
      padding: 12px 16px;
      color: white;
      font-family: 'Outfit', sans-serif;
      font-size: 0.95rem;
      outline: none;
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
      box-shadow: 0 0 20px rgba(243, 156, 18, 0.15);
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
            useExisting: forwardRef(() => PremiumInputComponent),
            multi: true
        }
    ]
})
export class PremiumInputComponent implements ControlValueAccessor {
    @Input() label: string = '';
    @Input() placeholder: string = '';
    @Input() type: string = 'text';
    @Input() icon: string = '';
    @Input() id: string = 'input-' + Math.random().toString(36).substring(2, 9);
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
