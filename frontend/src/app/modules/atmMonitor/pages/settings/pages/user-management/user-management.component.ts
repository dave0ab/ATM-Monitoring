import { Component, Inject, OnInit, ViewChild, OnDestroy } from '@angular/core';
import { AsyncPipe, NgClass, DatePipe } from '@angular/common';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import {
  MatTableModule,
  MatTable,
  MatTableDataSource,
} from '@angular/material/table';
import { MatSortModule, MatSort } from '@angular/material/sort';
import { MatPaginatorModule, MatPaginator } from '@angular/material/paginator';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { trigger, transition, style, animate } from '@angular/animations';
import {
  MatDialog,
  MatDialogRef,
  MAT_DIALOG_DATA,
  MatDialogModule,
} from '@angular/material/dialog';
import { MatSnackBar } from '@angular/material/snack-bar';
import { MatIcon, MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { FetchUsersState } from 'src/app/modules/auth/interfaces/user.interface';
import { Observable, Subject, merge } from 'rxjs';
import { Store } from '@ngrx/store';
import {
  selectFetchUsersState,
  selectUsersLoading,
  selectTotalElements,
  selectRegistrationLoading,
  selectRegistrationError,
  selectRegistrationSuccess,
  selectRegistrationValidationErrors,
  selectUpdateUserLoading,
  selectUpdateUserError,
  selectUpdateUserSuccess,
  selectUpdateUserValidationErrors,
} from 'src/app/modules/auth/store/auth.selectors';
import {
  clearRegistrationState,
  clearUpdateState,
  loadUsers,
  registerUser,
  updateUser,
} from 'src/app/modules/auth/store/auth.actions';
import { selectUsers } from 'src/app/modules/auth/store/auth.selectors';
import { Users } from 'src/app/modules/auth/interfaces/user.interface';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatTooltipModule } from '@angular/material/tooltip';
import { filter, takeUntil } from 'rxjs/operators';
import { Router } from '@angular/router';
@Component({
  selector: 'app-user-management',
  standalone: true,
  imports: [
    FormsModule,
    ReactiveFormsModule,
    MatTableModule,
    MatSortModule,
    MatPaginatorModule,
    MatIconModule,
    MatFormFieldModule,
    MatInputModule,
    MatProgressSpinnerModule,
    MatTooltipModule,
    MatButtonModule,
    MatDialogModule,
    AsyncPipe,
    NgClass,
  ],
  templateUrl: './user-management.component.html',
  animations: [
    trigger('fadeIn', [
      transition(':enter', [
        style({ opacity: 0 }),
        animate('300ms', style({ opacity: 1 })),
      ]),
    ]),
  ],
})
export class UserManagementComponent implements OnInit {
  getNotificationClass(level: string): string {
    switch (level) {
      case 'FIRST':
        return 'bg-green-500 text-white';
      case '2nd':
        return 'bg-green-300 text-black';
      case '3rd':
        return 'bg-yellow-300 text-black';
      case '4th':
        return 'bg-orange-300 text-black';
      case '5th':
        return 'bg-red-500 text-white';
      default:
        return 'bg-gray-200 text-black'; // Fallback for unknown levels
    }
  }

  users$ = this.store.select(selectUsers);
  loading$ = this.store.select(selectUsersLoading);
  totalItems$ = this.store.select(selectTotalElements);
  currentPage = 0;
  pageSize = 10;
  search = '';
  status: string = '';
  count: number = 0;

  @ViewChild(MatTable) table!: MatTable<Users>;
  @ViewChild(MatSort) sort!: MatSort;
  @ViewChild(MatPaginator) paginator!: MatPaginator;
  userColumns: string[] = [
    'domainId',
    'phoneNumber',
    'role',
    'status',
    'notificationLevel',
    'emailSubscribe',
    'actions',
  ];
  userForm: FormGroup;
  users: Users[] = [];
  darkMode = false;
  showAddUserModal = false;
  showConfirmDialog = false;
  userToDelete: Users | null = null;
  dataSource: MatTableDataSource<Users>;

  constructor(
    private fb: FormBuilder,
    private dialog: MatDialog,
    private snackBar: MatSnackBar,
    private store: Store,
    private router: Router
  ) {
    this.dataSource = new MatTableDataSource<Users>([]);
    this.userForm = this.fb.group({
      name: ['', [Validators.required, Validators.minLength(2)]],
      email: ['', [Validators.required, Validators.email]],
      role: ['', Validators.required],
      emailSubscribe: [false, Validators.required],
      notificationLevel: ['FIRST', Validators.required],
    });
  }

  showNotification(message: string) {
    this.snackBar.open(message, 'Close', {
      duration: 3000,
      horizontalPosition: 'right',
      verticalPosition: 'top',
    });
  }
  ngOnInit(): void {
    this.users$.pipe(filter((users) => !!users)).subscribe({
      next: (users) => {
        this.dataSource.data = users;
        this.table?.renderRows();
      },
    });

    this.loadUsers();
  }

  loadUsers() {
    this.store.dispatch(
      loadUsers({
        pageNumber: this.currentPage,
        pageSize: this.pageSize,
        search: this.search || '',
      })
    );
  }

  onPageChange(event: any) {
    this.currentPage = event.pageIndex;
    this.pageSize = event.pageSize;
    this.loadUsers();
  }

  applyFilter(event: Event) {
    const filterValue = (event.target as HTMLInputElement).value;
    this.search = filterValue.trim().toLowerCase();
    this.currentPage = 0;
    this.loadUsers();
  }

  // eslint-disable-next-line @angular-eslint/use-lifecycle-interface
  ngAfterViewInit() {
    this.dataSource.sort = this.sort;
  }

  addNewUser() {
    const dialogRef = this.dialog.open(UserFormDialogComponent, {
      width: '400px',
      data: {
        title: 'Add New User',
        user: {},
        isEdit: false,
      },
      disableClose: true,
    });

    dialogRef.afterClosed().subscribe((result) => {
      if (result === true) {
        this.loadUsers();
      }
    });
  }

  editUser(user: Users) {
    const dialogRef = this.dialog.open(UserFormDialogComponent, {
      width: '400px',
      data: {
        title: 'Edit User',
        user: { ...user }, // Create a copy of the user object
        isEdit: true,
      },
      disableClose: true,
      autoFocus: true,
    });

    // Handle dialog close
    dialogRef.afterClosed().subscribe((result) => {
      if (result === true) {
        this.loadUsers();
      }
    });
  }

  deleteUser(user: Users) {
    if (confirm(`Are you sure you want to delete ${user.domainId}?`)) {
      this.users = this.users.filter((u) => u.id !== user.id);
      this.dataSource.data = this.users;
      this.showNotification('User deleted successfully');
    }
  }

  onRowClick(user: Users): void {
    this.router.navigate([`dashboard/user-details/${user.id}`]);
  }
}

@Component({
  selector: 'app-user-form-dialog',
  standalone: true,
  imports: [
    MatDialogModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    MatButtonModule,
    ReactiveFormsModule,
    MatIconModule,
  ],
  template: `
    <div
      class="bg-white rounded-xl shadow-2xl overflow-hidden max-w-md w-full mx-auto"
    >
      <div class="bg-gradient-to-r from-rose-600 to-rose-200 p-6">
        <h2 class="text-3xl font-extrabold text-white mb-2">
          {{ data.title }}
        </h2>
        <p class="text-rose-100">Enter user details below</p>
      </div>

      <mat-dialog-content class="p-6">
        <form [formGroup]="userForm" class="space-y-6">
          <!-- Domain ID field -->
          <div class="relative">
            <label
              for="domainId"
              class="block text-sm font-medium text-gray-700 mb-1"
            >
              Domain Id
            </label>
            <div class="mt-1 relative rounded-md shadow-sm">
              <div
                class="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none"
              >
                <mat-icon class="text-gray-400">person</mat-icon>
              </div>
              <input
                type="text"
                id="domainId"
                formControlName="domainId"
                [readonly]="data.isEdit"
                class="block w-full pl-10 pr-3 py-2 border border-gray-300 rounded-md leading-5 bg-white placeholder-gray-500 focus:outline-none focus:ring-2 focus:ring-rose-600 focus:border-rose-600 sm:text-sm transition duration-150 ease-in-out"
                placeholder="Enter Domain"
                required
              />
            </div>
            @if ( userForm.get('domainId')!.invalid &&
            userForm.get('domainId')!.touched ) {
            <div class="text-red-500 text-sm mt-1">
              @if (userForm.get('domainId')!.errors?.['required']) {
              <div>Domain Id is required</div>
              } @if (userForm.get('domainId')!.errors?.['backend']) {
              <div>
                {{ userForm.get('domainId')!.errors?.['backend'] }}
              </div>
              }
            </div>
            }
          </div>

          <div class="relative">
            <label
              for="phoneNumber"
              class="block text-sm font-medium text-gray-700 mb-1"
            >
              Phone Number
            </label>
            <div class="mt-1 relative rounded-md shadow-sm">
              <div
                class="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none"
              >
                <mat-icon class="text-gray-400">phone</mat-icon>
              </div>
              <input
                type="text"
                id="phoneNumber"
                formControlName="phoneNumber"
                class="block w-full pl-10 pr-3 py-2 border border-gray-300 rounded-md leading-5 bg-white placeholder-gray-500 focus:outline-none focus:ring-2 focus:ring-rose-600 focus:border-rose-600 sm:text-sm transition duration-150 ease-in-out"
                placeholder="Enter Phone Number"
                required
              />
            </div>
            @if ( userForm.get('phoneNumber')!.invalid &&
            userForm.get('phoneNumber')!.touched ) {
            <div class="text-red-500 text-sm mt-1">
              @if (userForm.get('phoneNumber')!.errors?.['required']) {
              <div>Phone Number is required</div>
              } @if (userForm.get('phoneNumber')!.errors?.['backend']) {
              <div>
                {{ userForm.get('phoneNumber')!.errors?.['backend'] }}
              </div>
              }
            </div>
            }
          </div>

          <!-- Role field -->
          <div class="relative">
            <label
              for="role"
              class="block text-sm font-medium text-gray-700 mb-1"
            >
              Role
            </label>
            <div class="mt-1 relative rounded-md shadow-sm">
              <select
                id="role"
                formControlName="role"
                class="block w-full pl-3 pr-10 py-2 text-base border border-gray-300 focus:outline-none focus:ring-rose-600 focus:border-rose-600 sm:text-sm rounded-md"
                required
              >
                <option value="" disabled selected>Select a role</option>

                <option value="IT">IT</option>
                <option value="OMNI_CHANNEL">Omni Channel</option>
                <option value="BRANCH">Branch</option>
                <option value="ADMIN">Administrator</option>
                <option value="OMNI_CHANNEL_RECONCILIATION">
                  Omni Channel Reconciliation
                </option>
              </select>
              <div
                class="absolute inset-y-0 right-0 flex items-center px-2 pointer-events-none"
              >
                <mat-icon class="text-gray-400">expand_more</mat-icon>
              </div>
            </div>
            @if ( userForm.get('role')!.invalid && userForm.get('role')!.touched
            ) {
            <div class="text-red-500 text-sm mt-1">Please select a role</div>
            }
          </div>

          <!-- Status field - only show in edit mode -->
          @if (data.isEdit) {
          <div class="relative">
            <label
              for="status"
              class="block text-sm font-medium text-gray-700 mb-1"
            >
              Status
            </label>
            <div class="mt-1 relative rounded-md shadow-sm">
              <select
                id="status"
                formControlName="status"
                class="block w-full pl-3 pr-10 py-2 text-base border border-gray-300 focus:outline-none focus:ring-rose-600 focus:border-rose-600 sm:text-sm rounded-md"
                required
              >
                <option value="" disabled selected>Select a status</option>
                <option value="ACTIVE">Active</option>
                <option value="INACTIVE">Inactive</option>
              </select>
              <div
                class="absolute inset-y-0 right-0 flex items-center px-2 pointer-events-none"
              >
                <mat-icon class="text-gray-400">expand_more</mat-icon>
              </div>
            </div>
          </div>
          }

          <div class="relative">
            <label
              for="notificationLevel"
              class="block text-sm font-medium text-gray-700 mb-1"
            >
              Notification Level
            </label>
            <div class="mt-1 relative rounded-md shadow-sm">
              <select
                id="notificationLevel"
                formControlName="notificationLevel"
                class="block w-full pl-3 pr-10 py-2 text-base border border-gray-300 focus:outline-none focus:ring-rose-600 focus:border-rose-600 sm:text-sm rounded-md"
                required
              >
                <option value="FIRST">FIRST</option>
                <option value="SECOND">SECOND</option>
                <option value="THIRD">THIRD</option>
                <option value="FORTH">FORTH</option>
                <option value="FIFTH">FIFTH</option>
              </select>
              <div
                class="absolute inset-y-0 right-0 flex items-center px-2 pointer-events-none"
              >
                <mat-icon class="text-gray-400">expand_more</mat-icon>
              </div>
            </div>
            @if ( userForm.get('notificationLevel')!.invalid &&
            userForm.get('notificationLevel')!.touched ) {
            <div class="text-red-500 text-sm mt-1">
              @if (userForm.get('notificationLevel')!.errors?.['required']) {
              <div>Please select a notification level</div>
              } @if (userForm.get('notificationLevel')!.errors?.['backend']) {
              <div>
                {{ userForm.get('notificationLevel')!.errors?.['backend'] }}
              </div>
              }
            </div>
            }
          </div>

          <div class="relative">
            <!-- Checkbox for Email Subscription -->
            <div class="flex items-center mb-4">
              <input
                type="checkbox"
                id="emailSubscribe"
                formControlName="emailSubscribe"
                class="h-4 w-4 text-rose-600 focus:ring-rose-600 border-gray-300 rounded"
              />
              <label
                for="emailSubscribe"
                class="ml-2 block text-sm font-medium text-gray-700"
              >
                Subscribe to Email
              </label>
            </div>
            @if ( userForm.get('emailSubscribe')!.invalid &&
            userForm.get('emailSubscribe')!.touched ) {
            <div class="text-red-500 text-sm mt-1">
              Email subscription preference is required
            </div>
            }
          </div>
        </form>
      </mat-dialog-content>

      <mat-dialog-actions
        align="end"
        class="bg-gray-50 px-6 py-4 flex justify-end space-x-4"
      >
        <button
          mat-button
          (click)="onCancel()"
          class="px-6 py-2 rounded-lg text-gray-600 hover:bg-gray-200 transition-colors duration-200 focus:outline-none focus:ring-2 focus:ring-gray-300"
        >
          Cancel
        </button>
        <button
          mat-raised-button
          color="primary"
          (click)="onSubmit()"
          [disabled]="userForm.invalid"
          class="px-6 py-2 bg-gradient-to-r from-rose-600 to-indigo-600 text-white font-semibold rounded-lg shadow-md transition-all duration-200 ease-in-out transform hover:scale-105 focus:outline-none focus:ring-2 focus:ring-rose-500 focus:ring-opacity-50 disabled:opacity-50 disabled:cursor-not-allowed disabled:transform-none"
        >
          Save
        </button>
      </mat-dialog-actions>
    </div>
  `,
})
export class UserFormDialogComponent implements OnInit, OnDestroy {
  userForm: FormGroup;
  registrationLoading$ = this.store.select(selectRegistrationLoading);
  registrationError$ = this.store.select(selectRegistrationError);
  registrationSuccess$ = this.store.select(selectRegistrationSuccess);
  registrationValidationErrors$ = this.store.select(
    selectRegistrationValidationErrors
  );

  updateLoading$ = this.store.select(selectUpdateUserLoading);
  updateError$ = this.store.select(selectUpdateUserError);
  updateSuccess$ = this.store.select(selectUpdateUserSuccess);
  updateValidationErrors$ = this.store.select(selectUpdateUserValidationErrors);

  private destroy$ = new Subject<void>();

  constructor(
    public dialogRef: MatDialogRef<UserFormDialogComponent>,
    @Inject(MAT_DIALOG_DATA)
    public data: {
      title: string;
      user: Partial<Users>;
      isEdit: boolean;
    },
    private fb: FormBuilder,
    private store: Store,
    private snackBar: MatSnackBar
  ) {
    // Initialize form based on mode
    if (data.isEdit) {
      this.userForm = this.fb.group({
        domainId: [
          { value: data.user.domainId || '', disabled: true },
          Validators.required,
        ],
        role: [data.user.role || '', Validators.required],
        emailSubscribe: [
          data.user.emailSubscribe || false,
          Validators.required,
        ],
        status: [data.user.status || 'ACTIVE', Validators.required],
        notificationLevel: [
          data.user.notificationLevel || 'FIRST',
          Validators.required,
        ],
        phoneNumber: [data.user.phoneNumber || '', Validators.required],
      });
    } else {
      // Add mode - only domainId and role
      this.userForm = this.fb.group({
        domainId: ['', Validators.required],
        role: ['', Validators.required],
        emailSubscribe: [false, Validators.required],
        notificationLevel: ['FIRST', Validators.required],
        phoneNumber: ['', Validators.required],
      });
    }
  }

  // Method to get field-specific validation error
  getFieldError(fieldName: string): string | null {
    const validationErrors = this.data.isEdit
      ? this.updateValidationErrors$
      : this.registrationValidationErrors$;

    let fieldError: string | null = null;
    validationErrors
      .subscribe((errors) => {
        fieldError = errors && errors[fieldName] ? errors[fieldName] : null;
      })
      .unsubscribe();

    return fieldError;
  }

  // Method to check if field has validation error
  hasFieldError(fieldName: string): boolean {
    return this.getFieldError(fieldName) !== null;
  }

  ngOnInit() {
    // Handle registration success
    this.registrationSuccess$
      .pipe(
        takeUntil(this.destroy$),
        filter((success) => success !== null)
      )
      .subscribe(() => {
        this.snackBar.open('User registered successfully', 'Close', {
          duration: 3000,
          horizontalPosition: 'right',
          verticalPosition: 'top',
          panelClass: ['success-snackbar'],
        });
        this.store.dispatch(clearRegistrationState());
        this.dialogRef.close(true);
      });

    // Handle update success
    this.updateSuccess$
      .pipe(
        takeUntil(this.destroy$),
        filter((success) => success !== null)
      )
      .subscribe((success) => {
        // Show success toast
        this.snackBar.open('User updated successfully', 'Close', {
          duration: 3000,
          horizontalPosition: 'right',
          verticalPosition: 'top',
          panelClass: ['success-snackbar'],
        });
        // Close modal and clear state
        this.dialogRef.close(true);
        this.store.dispatch(clearUpdateState());
      });

    // Handle registration error
    this.registrationError$
      .pipe(
        takeUntil(this.destroy$),
        filter((error) => error !== null)
      )
      .subscribe((error) => {
        this.snackBar.open(error || 'Registration failed', 'Close', {
          duration: 5000,
          horizontalPosition: 'right',
          verticalPosition: 'top',
          panelClass: ['error-snackbar'],
        });
      });

    // Handle update error
    this.updateError$
      .pipe(
        takeUntil(this.destroy$),
        filter((error) => error !== null)
      )
      .subscribe((error) => {
        this.snackBar.open(error || 'Update failed', 'Close', {
          duration: 5000,
          horizontalPosition: 'right',
          verticalPosition: 'top',
          panelClass: ['error-snackbar'],
        });
      });

    // Handle registration validation errors
    this.registrationValidationErrors$
      .pipe(
        takeUntil(this.destroy$),
        filter((errors) => errors !== null)
      )
      .subscribe((errors) => {
        if (errors) {
          // Mark form fields as invalid and set custom error messages
          Object.keys(errors).forEach((fieldName) => {
            const control = this.userForm.get(fieldName);
            if (control) {
              control.setErrors({ backend: errors[fieldName] });
              control.markAsTouched();
            }
          });
        }
      });

    // Handle update validation errors
    this.updateValidationErrors$
      .pipe(
        takeUntil(this.destroy$),
        filter((errors) => errors !== null)
      )
      .subscribe((errors) => {
        if (errors) {
          // Mark form fields as invalid and set custom error messages
          Object.keys(errors).forEach((fieldName) => {
            const control = this.userForm.get(fieldName);
            if (control) {
              control.setErrors({ backend: errors[fieldName] });
              control.markAsTouched();
            }
          });
        }
      });
  }

  onSubmit(): void {
    // Clear any previous backend validation errors
    Object.keys(this.userForm.controls).forEach((key) => {
      const control = this.userForm.get(key);
      if (control && control.errors?.['backend']) {
        const errors = { ...control.errors };
        delete errors['backend'];
        control.setErrors(Object.keys(errors).length ? errors : null);
      }
    });

    if (this.userForm.valid) {
      if (this.data.isEdit) {
        const updateRequest = {
          domainId: this.userForm.getRawValue().domainId,
          role: this.userForm.value.role,
          status: this.userForm.value.status,
          emailSubscribe: this.userForm.value.emailSubscribe === true,
          notificationLevel: this.userForm.value.notificationLevel,
          phoneNumber: this.userForm.value.phoneNumber,
        };

        this.store.dispatch(
          updateUser({
            userId: this.data.user.id!,
            request: updateRequest,
          })
        );
      } else {
        const request = {
          domainId: this.userForm.value.domainId,
          role: this.userForm.value.role,
          notificationLevel: this.userForm.value.notificationLevel,
          emailSubscribe: this.userForm.value.emailSubscribe === true,
          phoneNumber: this.userForm.value.phoneNumber,
        };
        this.store.dispatch(registerUser({ request }));
      }
    }
  }

  onCancel(): void {
    this.dialogRef.close(false);
  }

  ngOnDestroy() {
    this.destroy$.next();
    this.destroy$.complete();
    // Clear states on destroy
    if (this.data.isEdit) {
      this.store.dispatch(clearUpdateState());
    } else {
      this.store.dispatch(clearRegistrationState());
    }
  }
}
