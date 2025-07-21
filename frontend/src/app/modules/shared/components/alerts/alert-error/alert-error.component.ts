import { Component, Input } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-alert-error',
  standalone: true,
  imports: [CommonModule],
  template: `
<div class="bg-red-50 rounded-md p-3 flex" [ngClass]="class">
  <svg
    class="text-red-400 h-8 w-8 mr-2 flex-shrink-0"
    xmlns="http://www.w3.org/2000/svg"
    viewBox="0 0 20 20"
    fill="currentColor"
    aria-hidden="true">
    <path
      fill-rule="evenodd"
      clip-rule="evenodd"
      d="M10 18a8 8 0 1 0 0-16 8 8 0 0 0 0 16ZM8.28 7.22a.75.75 0 0 0-1.06 1.06L8.94 10l-1.72 1.72a.75.75 0 1 0 1.06 1.06L10 11.06l1.72 1.72a.75.75 0 1 0 1.06-1.06L11.06 10l1.72-1.72a.75.75 0 0 0-1.06-1.06L10 8.94 8.28 7.22Z" />
    </svg>
    <div class="text-red-700">
      <div class="font-bold text-xl">Error!</div>

      <div>{{ message }}</div>
      <ul role="list" class="pl-5 space-y-1 list-disc">
        @for (error of errors; track error) {
          <li>{{ error }}</li>
        }
      </ul>
    </div>
  </div>

`,
  styleUrls: ['./alert-error.component.scss'],
})
export class AlertErrorComponent {
  @Input() class!: string;
  @Input() message!: string;
  @Input() errors: string[] = [];
}
