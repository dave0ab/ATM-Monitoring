import { Component, Inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import {
  MatDialogRef,
  MAT_DIALOG_DATA,
  MatDialogModule,
} from '@angular/material/dialog';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { UserProfile } from 'src/app/modules/dashboard/interfaces/Dashboard.intreface';

@Component({
  selector: 'app-profile-dialog',
  standalone: true,
  imports: [CommonModule, MatDialogModule, MatButtonModule, MatIconModule],
  template: `
    <div class="w-full max-w-2xl">
      <div class="relative p-6">
        <!-- Header -->
        <div class="flex justify-between items-center mb-6">
          <h2 class="text-2xl font-semibold text-gray-800">Profile</h2>
          <button
            mat-icon-button
            (click)="close()"
            class="text-gray-500 hover:text-gray-700"
            >
            <mat-icon>close</mat-icon>
          </button>
        </div>
    
        <!-- Profile Content -->
        @if (data) {
          <div class="space-y-6">
            <!-- Profile Image and Basic Info -->
            <div class="flex items-start space-x-6">
              <div class="flex-shrink-0">
                <div
                  class="w-24 h-24 rounded-full bg-rose-100 flex items-center justify-center"
                  >
                  <span class="text-3xl font-bold text-rose-600">
                    {{ getInitials(data.domainId) }}
                  </span>
                </div>
              </div>
              <div class="flex-1">
                <h3
                  class="text-xl font-semibold text-gray-800"
                  style="text-transform: uppercase;"
                  >
                  {{ data.domainId || 'N/A' }}
                </h3>
                <p class="text-gray-600">{{ data.role || 'N/A' }}</p>
                <p class="text-sm text-gray-500 mt-1">
                  Last Sign On: {{ data.lastSignOn | date : 'medium' }}
                </p>
              </div>
            </div>
            <!-- Account Status -->
            <div class="bg-gray-50 p-4 rounded-lg">
              <h4 class="text-lg font-medium text-gray-800 mb-3">
                Account Status
              </h4>
              <div class="space-y-2">
                <div class="flex items-center text-gray-600">
                  <mat-icon class="text-gray-400 mr-2">info</mat-icon>
                  <span>Status: {{ data.status }}</span>
                </div>
                <div class="flex items-center text-gray-600">
                  <mat-icon class="text-gray-400 mr-2">notifications</mat-icon>
                  <span>Notification Level: {{ data.notificationLevel }}</span>
                </div>
              </div>
            </div>
            <!-- Account Details -->
            <div class="bg-gray-50 p-4 rounded-lg">
              <h4 class="text-lg font-medium text-gray-800 mb-3">
                Account Details
              </h4>
              <div class="space-y-2">
                <div class="flex items-center text-gray-600">
                  <mat-icon class="text-gray-400 mr-2">calendar_today</mat-icon>
                  <span>Created: {{ data.createdAt | date : 'medium' }}</span>
                </div>
                <div class="flex items-center text-gray-600">
                  <mat-icon class="text-gray-400 mr-2">login</mat-icon>
                  <span>Last Login: {{ data.lastSignOn | date : 'medium' }}</span>
                </div>
              </div>
            </div>
          </div>
        }
    
        <!-- Footer -->
        <div class="mt-8 flex justify-end space-x-3">
          <button
            mat-button
            (click)="close()"
            class="px-4 py-2 text-gray-700 hover:bg-gray-100 rounded-lg"
            >
            Close
          </button>
        </div>
      </div>
    </div>
    `,
})
export class ProfileDialogComponent {
  constructor(
    private dialogRef: MatDialogRef<ProfileDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public data: UserProfile
  ) {}

  getInitials(domainId: string): string {
    if (!domainId) return 'NA';
    const parts = domainId.split('.');
    if (parts.length >= 2) {
      return (parts[0][0] + parts[1][0]).toUpperCase();
    }
    return domainId.substring(0, 2).toUpperCase();
  }

  close(): void {
    this.dialogRef.close();
  }
}
