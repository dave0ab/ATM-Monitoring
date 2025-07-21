import { Component, Inject, OnInit } from '@angular/core';
import { DatePipe } from '@angular/common';
import { MatTabsModule } from '@angular/material/tabs';
import { MatTableModule } from '@angular/material/table';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import {
  MatDialogModule,
  MatDialog,
  MAT_DIALOG_DATA,
  MatDialogRef,
} from '@angular/material/dialog';
import {
  FormBuilder,
  FormGroup,
  FormsModule,
  ReactiveFormsModule,
  Validators,
} from '@angular/forms';
import { MatSnackBarModule, MatSnackBar } from '@angular/material/snack-bar';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatSlideToggleModule } from '@angular/material/slide-toggle';
import {
  UserFormDialogComponent,
  UserManagementComponent,
} from './pages/user-management/user-management.component';
import { forwardRef } from '@angular/core';
import { User } from 'src/app/modules/auth/interfaces/user.interface';
import { AtmLocationManagementComponent } from './pages/Atm-Location-Management/atm-location-management.component';

interface Role {
  id: number;
  name: string;
  description: string;
  permissions: string[];
}

@Component({
  selector: 'app-settings',
  standalone: true,
  imports: [
    MatTabsModule,
    MatTableModule,
    MatButtonModule,
    MatIconModule,
    MatInputModule,
    MatSelectModule,
    MatDialogModule,
    MatSnackBarModule,
    FormsModule,
    ReactiveFormsModule,
    MatSlideToggleModule,
    UserManagementComponent,
    AtmLocationManagementComponent
],
  providers: [DatePipe],
  templateUrl: './settings.component.html',
  styles: [
    `
      ::ng-deep .settings-tabs {
        .mat-mdc-tab-group {
          /* Selected tab text color */
          .mat-mdc-tab:not(.mat-mdc-tab-disabled).mdc-tab--active
            .mdc-tab__text-label {
            color: #e11d48 !important;
          }

          /* Tab indicator color */
          .mat-mdc-tab-header {
            --mat-tab-active-indicator-color: #e11d48 !important;
            --mat-tab-active-ripple-color: #e11d48 !important;
            --mat-tab-inactive-ripple-color: #fecdd3 !important;
            --mat-tab-active-indicator-height: 2px !important;
            border-bottom: 1px solid #e5e7eb;
          }

          /* Hover effects */
          .mat-mdc-tab:not(.mat-mdc-tab-disabled):hover {
            background-color: #fff1f2;
          }

          /* Active tab */
          .mat-mdc-tab.mdc-tab--active {
            background-color: #ffe4e6;
          }

          /* Tab label styling */
          .mdc-tab__text-label {
            font-weight: 500;
          }

          /* Icon styling */
          .mat-icon {
            margin-right: 8px;
          }
        }
      }

      /* Table styles */
      ::ng-deep .mat-mdc-table {
        background: white;

        .mat-mdc-header-cell {
          font-weight: 600;
          color: #374151;
          background: #f9fafb;
          padding: 16px;
        }

        .mat-mdc-cell {
          padding: 16px;
          color: #4b5563;
        }

        .mat-mdc-row:hover {
          background-color: #f9fafb;
        }
      }
    `,
  ],
})
export class SettingsComponent implements OnInit {
  userColumns: string[] = ['username', 'email', 'role', 'status', 'actions'];
  roleColumns: string[] = ['name', 'description', 'permissions', 'actions'];

  roles: Role[] = [];

  constructor(private dialog: MatDialog, private snackBar: MatSnackBar) {}

  ngOnInit() {
    this.loadRoles();
  }

  loadRoles() {
    // Simulating API call
    this.roles = [
      {
        id: 1,
        name: 'Administrator',
        description: 'Full system access',
        permissions: ['Create', 'Read', 'Update', 'Delete'],
      },
      {
        id: 2,
        name: 'Operator',
        description: 'ATM management access',
        permissions: ['Read', 'Update'],
      },
      {
        id: 3,
        name: 'Viewer',
        description: 'View-only access',
        permissions: ['Read'],
      },
    ];
  }

  addNewRole() {
    const dialogRef = this.dialog.open(RoleFormDialogComponent, {
      width: '400px',
      data: { title: 'Add New Role', role: {} },
    });

    dialogRef.afterClosed().subscribe((result) => {
      if (result) {
        const newRole: Role = {
          id: this.roles.length + 1,
          ...result,
        };
        this.roles = [...this.roles, newRole];
        this.showNotification('Role added successfully');
      }
    });
  }

  editRole(role: Role) {
    const dialogRef = this.dialog.open(RoleFormDialogComponent, {
      width: '400px',
      data: { title: 'Edit Role', role: { ...role } },
    });

    dialogRef.afterClosed().subscribe((result) => {
      if (result) {
        this.roles = this.roles.map((r) =>
          r.id === role.id ? { ...r, ...result } : r
        );
        this.showNotification('Role updated successfully');
      }
    });
  }

  deleteRole(role: Role) {
    if (confirm(`Are you sure you want to delete the ${role.name} role?`)) {
      this.roles = this.roles.filter((r) => r.id !== role.id);
      this.showNotification('Role deleted successfully');
    }
  }

  showNotification(message: string) {
    this.snackBar.open(message, 'Close', {
      duration: 3000,
      horizontalPosition: 'right',
      verticalPosition: 'top',
    });
  }

  savePermissions() {
    // Implement permission saving logic
    this.showNotification('Permissions updated successfully');
  }
}

@Component({
  selector: 'app-role-form-dialog',
  standalone: true,
  imports: [
    MatDialogModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    MatButtonModule,
    ReactiveFormsModule,
  ],
  template: `
    <h2 mat-dialog-title class="text-2xl font-bold mb-4">{{ data.title }}</h2>
    <mat-dialog-content>
      <form [formGroup]="roleForm" class="space-y-4">
        <mat-form-field appearance="outline" class="w-full">
          <mat-label>Role Name</mat-label>
          <input matInput formControlName="name" required />
        </mat-form-field>
        <mat-form-field appearance="outline" class="w-full">
          <mat-label>Description</mat-label>
          <textarea matInput formControlName="description" required></textarea>
        </mat-form-field>
        <mat-form-field appearance="outline" class="w-full">
          <mat-label>Permissions</mat-label>
          <mat-select formControlName="permissions" multiple required>
            <mat-option value="Create">Create</mat-option>
            <mat-option value="Read">Read</mat-option>
            <mat-option value="Update">Update</mat-option>
            <mat-option value="Delete">Delete</mat-option>
          </mat-select>
        </mat-form-field>
      </form>
    </mat-dialog-content>
    <mat-dialog-actions align="end" class="mt-4">
      <button mat-button (click)="onCancel()" class="mr-2">Cancel</button>
      <button
        mat-raised-button
        color="primary"
        (click)="onSubmit()"
        [disabled]="roleForm.invalid"
        class="bg-blue-500 hover:bg-blue-600"
      >
        Save
      </button>
    </mat-dialog-actions>
  `,
})
export class RoleFormDialogComponent {
  roleForm: FormGroup;

  constructor(
    public dialogRef: MatDialogRef<RoleFormDialogComponent>,
    @Inject(MAT_DIALOG_DATA)
    public data: { title: string; role: Partial<Role> },
    private fb: FormBuilder
  ) {
    this.roleForm = this.fb.group({
      name: [data.role.name || '', Validators.required],
      description: [data.role.description || '', Validators.required],
      permissions: [data.role.permissions || [], Validators.required],
    });
  }

  onCancel(): void {
    this.dialogRef.close();
  }

  onSubmit(): void {
    if (this.roleForm.valid) {
      this.dialogRef.close(this.roleForm.value);
    }
  }
}
