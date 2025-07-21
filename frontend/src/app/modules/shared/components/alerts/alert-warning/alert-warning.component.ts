import { Component, Input } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-alert-warning',
  standalone: true,
  imports: [CommonModule],
  template: `
<div class="bg-yellow-50 rounded-md p-3 flex" [ngClass]="class">
  <svg   class="text-yellow-400 h-8 w-8 mr-2 flex-shrink-0"  fill="currentColor"  version="1.1" id="Capa_1" xmlns="http://www.w3.org/2000/svg" xmlns:xlink="http://www.w3.org/1999/xlink" viewBox="0 0 123.996 123.996" xml:space="preserve"><g id="SVGRepo_bgCarrier" stroke-width="0"></g><g id="SVGRepo_tracerCarrier" stroke-linecap="round" stroke-linejoin="round"></g><g id="SVGRepo_iconCarrier"> <g> <path d="M9.821,118.048h104.4c7.3,0,12-7.7,8.7-14.2l-52.2-92.5c-3.601-7.199-13.9-7.199-17.5,0l-52.2,92.5 C-2.179,110.348,2.521,118.048,9.821,118.048z M70.222,96.548c0,4.8-3.5,8.5-8.5,8.5s-8.5-3.7-8.5-8.5v-0.2c0-4.8,3.5-8.5,8.5-8.5 s8.5,3.7,8.5,8.5V96.548z M57.121,34.048h9.801c2.699,0,4.3,2.3,4,5.2l-4.301,37.6c-0.3,2.7-2.1,4.4-4.6,4.4s-4.3-1.7-4.6-4.4 l-4.301-37.6C52.821,36.348,54.422,34.048,57.121,34.048z"></path> </g> </g></svg>


  <div class="text-yellow-600">
    <div class="font-bold text-xl">Session!</div>

    <div>{{ message }}</div>
    <ul role="list" class="pl-5 space-y-1 list-disc">
      @for (error of errors; track error) {
        <li>{{ error }}</li>
      }
    </ul>
  </div>
</div>
`,
  styleUrls: ['./alert-warning.component.scss'],
})
export class AlertWarningComponent {
@Input() class!: string;
@Input() message!: string;
@Input() errors: string[] = [];
}
