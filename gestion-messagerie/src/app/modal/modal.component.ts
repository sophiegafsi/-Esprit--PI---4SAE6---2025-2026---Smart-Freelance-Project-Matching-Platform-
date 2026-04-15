import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ModalService, ModalOptions, ModalType } from '../services/modal.service';

@Component({
    selector: 'app-modal',
    standalone: true,
    imports: [CommonModule, FormsModule],
    templateUrl: './modal.component.html',
    styleUrl: './modal.component.css'
})
export class ModalComponent implements OnInit {
    options: ModalOptions | null = null;
    ModalType = ModalType;
    inputValue = '';

    constructor(private modalService: ModalService) { }

    ngOnInit(): void {
        this.modalService.modalState$.subscribe(state => {
            this.options = state;
            if (state && state.type === ModalType.PROMPT) {
                this.inputValue = state.defaultValue || '';
            }
        });
    }

    confirm() {
        if (this.options?.type === ModalType.PROMPT) {
            this.modalService.submit(this.inputValue);
        } else {
            this.modalService.submit(true);
        }
    }

    cancel() {
        this.modalService.cancel();
    }
}
