import { Component, Input } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatSidenavModule } from '@angular/material/sidenav';
import { RouterLink, RouterOutlet, RouterModule } from '@angular/router';

interface Notification {
  id: number;
  message: string;
  severity: string;
  time: string;
}

@Component({
  selector: 'app-alerts',
  standalone: true,

  templateUrl: './alerts.component.html',
  styleUrls: ['./alerts.component.scss'],

  imports: [
    CommonModule,
    FormsModule,
    MatButtonModule,
    MatIconModule,
    MatSidenavModule,
    MatProgressBarModule,
    RouterLink,
    RouterOutlet,
    RouterModule,
  ],
})
export class AlertsComponent {
  @Input() notifications: Notification[] = [];
}
