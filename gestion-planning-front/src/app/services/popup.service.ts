import { Injectable } from '@angular/core';

export type PopupType = 'success' | 'error' | 'warning' | 'info' | 'confirm';

export interface PopupState {
  isOpen: boolean;
  type: PopupType;
  title: string;
  message: string;
  confirmText: string;
  cancelText: string;
  onConfirm?: () => void;
  onCancel?: () => void;
}

@Injectable({
  providedIn: 'root'
})
export class PopupService {
  popup: PopupState = {
    isOpen: false,
    type: 'info',
    title: '',
    message: '',
    confirmText: 'OK',
    cancelText: 'Cancel',
    onConfirm: undefined,
    onCancel: undefined
  };

  show(
    type: PopupType,
    title: string,
    message: string,
    options?: {
      confirmText?: string;
      cancelText?: string;
      onConfirm?: () => void;
      onCancel?: () => void;
    }
  ): void {
    this.popup.isOpen = true;
    this.popup.type = type;
    this.popup.title = title;
    this.popup.message = message;
    this.popup.confirmText = options?.confirmText ?? 'OK';
    this.popup.cancelText = options?.cancelText ?? 'Cancel';
    this.popup.onConfirm = options?.onConfirm;
    this.popup.onCancel = options?.onCancel;
  }

  success(title: string, message: string): void {
    this.show('success', title, message);
  }

  error(title: string, message: string): void {
    this.show('error', title, message);
  }

  confirm(
    title: string,
    message: string,
    onConfirm: () => void,
    onCancel?: () => void
  ): void {
    this.show('confirm', title, message, {
      confirmText: 'OK',
      cancelText: 'Cancel',
      onConfirm,
      onCancel
    });
  }

  close(): void {
    this.popup.isOpen = false;
    this.popup.title = '';
    this.popup.message = '';
    this.popup.onConfirm = undefined;
    this.popup.onCancel = undefined;
    this.popup.confirmText = 'OK';
    this.popup.cancelText = 'Cancel';
    this.popup.type = 'info';
  }

  handleConfirm(): void {
    const callback = this.popup.onConfirm;
    this.close();
    if (callback) callback();
  }

  handleCancel(): void {
    const callback = this.popup.onCancel;
    this.close();
    if (callback) callback();
  }
}