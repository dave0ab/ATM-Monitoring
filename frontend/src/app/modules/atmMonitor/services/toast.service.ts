import { Injectable } from '@angular/core';
import { BehaviorSubject } from 'rxjs';

@Injectable({
  providedIn: 'root',
})
export class ToastService {
  private successMessageSource = new BehaviorSubject<string | null>(null);
  private errorMessageSource = new BehaviorSubject<string | null>(null);

  successMessage$ = this.successMessageSource.asObservable();
  errorMessage$ = this.errorMessageSource.asObservable();

  showSuccess(message: string, duration: number = 5000) {
    this.successMessageSource.next(message);
    setTimeout(() => {
      this.clearMessages();
    }, duration);
  }

  showError(message: string, duration: number = 5000) {
    this.errorMessageSource.next(message);
    setTimeout(() => {
      this.clearMessages();
    }, duration);
  }

  showWarning(message: string) {
    // Implementation
  }

  clearMessages() {
    this.successMessageSource.next(null);
    this.errorMessageSource.next(null);
  }
}
