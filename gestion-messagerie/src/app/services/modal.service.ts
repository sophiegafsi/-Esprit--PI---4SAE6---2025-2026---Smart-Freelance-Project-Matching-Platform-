import { Injectable } from '@angular/core';
import { Subject, Observable } from 'rxjs';

export enum ModalType {
    CONFIRM = 'CONFIRM',
    PROMPT = 'PROMPT',
    ALERT = 'ALERT'
}



export interface ModalOptions {
    title?: string;
    message: string;
    type: ModalType;
    defaultValue?: string;
    confirmText?: string;
    cancelText?: string;
}

@Injectable({
    providedIn: 'root'
})
export class ModalService {
    private modalSubject = new Subject<ModalOptions | null>();
    private resultSubject = new Subject<any>();

    modalState$ = this.modalSubject.asObservable();

    confirm(message: string, title: string = 'Confirm'): Observable<boolean> {
        this.modalSubject.next({
            title,
            message,
            type: ModalType.CONFIRM,
            confirmText: 'Confirm',
            cancelText: 'Cancel'
        });
        return this.resultSubject.asObservable();
    }

    prompt(message: string, defaultValue: string = '', title: string = 'Input Required'): Observable<string | null> {
        this.modalSubject.next({
            title,
            message,
            type: ModalType.PROMPT,
            defaultValue,
            confirmText: 'Save',
            cancelText: 'Cancel'
        });
        return this.resultSubject.asObservable();
    }

    alert(message: string, title: string = 'Notification'): Observable<void> {
        this.modalSubject.next({
            title,
            message,
            type: ModalType.ALERT,
            confirmText: 'OK'
        });
        return this.resultSubject.asObservable();
    }

    submit(result: any) {
        this.modalSubject.next(null);
        this.resultSubject.next(result);
    }

    cancel() {
        this.modalSubject.next(null);
        this.resultSubject.next(null);
    }
}
