import { SelectionModel } from '@angular/cdk/collections';
import { DatePipe, NgClass, NgStyle, AsyncPipe } from '@angular/common';
import { HttpClientModule } from '@angular/common/http';
import { Component, OnInit, OnDestroy, ViewChild, Inject } from '@angular/core';
import { FormBuilder, FormsModule, ReactiveFormsModule } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatMenuModule } from '@angular/material/menu';
import {
  MatPaginator,
  MatPaginatorModule,
  PageEvent,
} from '@angular/material/paginator';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatSelectModule } from '@angular/material/select';
import { MatSortModule } from '@angular/material/sort';
import { MatTableDataSource, MatTableModule } from '@angular/material/table';
import { DomSanitizer } from '@angular/platform-browser';
import { ActivatedRoute, Router } from '@angular/router';
import { Store } from '@ngrx/store';
import { NgxDaterangepickerMd } from 'ngx-daterangepicker-material';
import {
  Observable,
  Subject,
  filter,
  takeUntil,
  map,
  distinctUntilChanged,
  catchError,
  tap,
  EMPTY,
} from 'rxjs';
import { AlertErrorComponent } from 'src/app/modules/shared/components/alerts/alert-error/alert-error.component';
import { AlertWarningComponent } from 'src/app/modules/shared/components/alerts/alert-warning/alert-warning.component';
import { PrimaryComponent } from 'src/app/modules/shared/components/buttons/primary.component';
import { InputComponent } from 'src/app/modules/shared/components/inputs/input/input.component';
import { LoadingComponent } from 'src/app/modules/shared/components/loading/loading.component';
import { Atm, FetchAtmListState } from '../../interfaces/atm.interface';
import { LoadingService } from '../../services/loading.service';
import { ToastService } from '../../services/toast.service';
import {
  selectAtmState,
  selectAtmLoading,
  selectAtmTotalItems,
  selectAtmError,
} from '../../store/atm/atm.selectors';
import { fetchAtm } from '../../store/atm/atm.actions';
import {
  MatDialog,
  MatDialogRef,
  MAT_DIALOG_DATA,
  MatDialogModule,
} from '@angular/material/dialog';
import * as AtmAccessActions from '../../store/access/access.actions';
import * as AtmAccessSelectors from '../../store/access/access.selectors';
import { AtmAccess } from '../../interfaces/atm-access.interface';
import { saveAtmAccess } from '../../store/access/access.actions';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { selectAtmAccesses } from '../../store/access/access.selectors';
import { DashboardService } from 'src/app/modules/dashboard/services/dashboard.service';
import { UserProfile } from 'src/app/modules/dashboard/interfaces/Dashboard.intreface';

interface UserDetails {
  id: number;
  name: string;
  role: string;
  profileImage: string;
}

interface Permission {
  name: string;
  description: string;
  enabled: boolean;
}

interface Activity {
  id: number;
  action: string;
  timestamp: Date;
  details: string;
}

@Component({
  standalone: true,
  imports: [
    FormsModule,
    ReactiveFormsModule,
    MatTableModule,
    MatPaginatorModule,
    MatSortModule,
    MatInputModule,
    MatSelectModule,
    MatCheckboxModule,
    MatButtonModule,
    MatIconModule,
    MatMenuModule,
    NgxDaterangepickerMd,
    MatProgressBarModule,
    HttpClientModule,
    AlertErrorComponent,
    FormsModule,
    MatDialogModule,
    MatProgressSpinnerModule,
    NgClass,
    NgStyle,
    AsyncPipe,
    DatePipe,
  ],
  providers: [DatePipe],
  selector: 'app-user-profile',
  templateUrl: './user-details.component.html',
  styleUrls: ['./user-details.component.scss'],
})
export class UserDetailsComponent implements OnInit, OnDestroy {
  onHeaderClick(column: string): void {
    // Implement sorting logic here if needed
  }

  onRowClick(atm: Atm): void {
    // Implement row click logic here if needed
  }

  onPageChange(event: PageEvent): void {
    this.currentPage = event.pageIndex + 1;
    this.pageSize = event.pageSize;
    this.loadATMS();
  }
  userId: number = 0;
  activeTab: 'overview' | 'assign-ATMS' = 'overview';
  userDetails: UserDetails | null = null;
  permissions: Permission[] = [];
  activities: Activity[] = [];
  dataSource = new MatTableDataSource<Atm>([]);
  selection = new SelectionModel<Atm>(true, []);
  fetch$: Observable<FetchAtmListState> = this.store.select(selectAtmState);
  loading$: Observable<boolean> = this.store.select(selectAtmLoading);
  loading: boolean = true;
  totalItems$: Observable<number | null> =
    this.store.select(selectAtmTotalItems);
  isStopedLoading = true;
  currentPage = 1;
  pageSize = 10;
  search = '';
  status = '';
  count: number = 0;
  sort = '';
  sortBy = '';
  problem = '';
  public error$: Observable<string | null> = this.store.select(selectAtmError);
  displayedColumns: string[] = ['select', 'atmId', 'atmName'];
  private destroy$ = new Subject<void>();
  atms: Atm[] = [];

  hasChanges: boolean = false;
  atmAccessLoading$ = this.store.select(
    AtmAccessSelectors.selectAtmAccessLoading
  );
  atmAccessError$ = this.store.select(AtmAccessSelectors.selectAtmAccessError);
  atmAccessSuccess$ = this.store.select(
    AtmAccessSelectors.selectAtmAccessSuccess
  );
  initialSelection: Set<string> = new Set();
  userProfile: UserProfile | null = null;
  error: string | null = null;

  constructor(
    private store: Store,
    private router: Router,
    private _formBuilder: FormBuilder,
    private toastService: ToastService,
    private loadingService: LoadingService,
    private route: ActivatedRoute,
    private datePipe: DatePipe,
    private sanitizer: DomSanitizer,
    private dialog: MatDialog,
    private dashboardService: DashboardService
  ) {
    this.fetch$ = this.store.select(selectAtmState);
    this.loading$ = this.store.select(selectAtmLoading);
    this.totalItems$ = this.store.select(selectAtmTotalItems);
  }
  @ViewChild(MatPaginator) paginator!: MatPaginator;

  ngOnInit(): void {
    this.activeTab = 'overview';

    // Handle route params with error catching
    this.route.paramMap
      .pipe(
        takeUntil(this.destroy$),
        filter((params) => {
          const hasId = params.has('id');

          return hasId;
        }),
        map((params) => {
          const id = +params.get('id')!;

          return id;
        }),
        distinctUntilChanged(),
        catchError((err) => {
          this.error = 'Failed to load user details';
          return EMPTY;
        })
      )
      .subscribe({
        next: (userId) => {
          this.userId = userId;
          this.resetState();
          this.loadUserProfile();
        },
        error: (err) => {
          this.error = 'Failed to process user details';
        },
      });

    // Add error handling to data fetching
    this.fetch$
      .pipe(
        takeUntil(this.destroy$),
        filter((response): response is FetchAtmListState => {
          return !!response?.atm;
        }),
        catchError((err) => {
          this.error = 'Failed to load ATM data';
          return EMPTY;
        })
      )
      .subscribe({
        next: (response) => {
          if (response.atm?.content) {
            this.atms = response.atm.content;
            this.dataSource.data = this.sortWithSelectedOnTop(
              response.atm.content
            );

            if (this.activeTab === 'assign-ATMS') {
              this.loadAtmAccess();
            }
          }
        },
        error: (err) => {
          this.error = 'Failed to process ATM data';
        },
      });

    // Add cleanup on destroy
    this.destroy$.subscribe();

    // Handle ATM access
    this.store
      .select(selectAtmAccesses)
      .pipe(
        takeUntil(this.destroy$),
        filter((accesses) => !!accesses)
      )
      .subscribe((atmAccesses) => {
        if (atmAccesses && this.dataSource.data.length) {
          this.updateSelections(atmAccesses);
        }
      });

    this.loading$
      .pipe(
        takeUntil(this.destroy$),
        filter((value): value is boolean => typeof value === 'boolean')
      )
      .subscribe((isLoading) => {
        this.isStopedLoading = isLoading;
      });

    this.totalItems$
      .pipe(
        takeUntil(this.destroy$),
        filter(
          (value): value is number =>
            typeof value === 'number' || value === null
        )
      )
      .subscribe((total) => {
        this.count = total || 0;
      });

    this.atmAccessSuccess$
      .pipe(takeUntil(this.destroy$))
      .subscribe((success) => {
        if (success) {
          this.toastService.showSuccess(success);
          // Reload ATMs after successful save
          this.loadATMS();
        }
      });

    this.atmAccessError$.pipe(takeUntil(this.destroy$)).subscribe((error) => {
      if (error) {
        this.toastService.showError(error);
      }
    });
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
    this.selection.clear();
    this.dataSource.data = [];
  }

  loadATMS(): void {
    if (!this.userId) return;

    // Clear existing selections
    this.selection.clear();
    this.initialSelection.clear();

    this.store.dispatch(
      fetchAtm({
        pageNumber: this.currentPage - 1,
        pageSize: this.pageSize,
        status: this.status || '',
        search: this.search || '',
        sort: this.sort || 'atmName',
        sortBy: this.sortBy || 'atmName',
        problem: this.problem || '',
      })
    );
  }

  setActiveTab(tab: 'overview' | 'assign-ATMS'): void {
    this.activeTab = tab;
    if (tab === 'assign-ATMS') {
      this.loadATMS();
    }
  }

  fetchUserDetails(): void {
    // Simulating an API call to fetch user details
    setTimeout(() => {
      this.userDetails = {
        id: this.userId,
        name: 'John Doe',
        role: 'Administrator',
        profileImage: 'https://randomuser.me/api/portraits/men/1.jpg',
      };
    }, 500);
  }

  fetchPermissions(): void {
    // Simulating an API call to fetch permissions
    setTimeout(() => {
      this.permissions = [
        { name: 'Read', description: 'Can view content', enabled: true },
        {
          name: 'Write',
          description: 'Can create and edit content',
          enabled: false,
        },
        { name: 'Delete', description: 'Can delete content', enabled: false },
        {
          name: 'Admin',
          description: 'Full administrative access',
          enabled: true,
        },
      ];
    }, 500);
  }

  fetchActivities(): void {
    // Simulating an API call to fetch activities
    setTimeout(() => {
      this.activities = [
        {
          id: 1,
          action: 'Login',
          timestamp: new Date('2023-04-20T10:30:00'),
          details: 'Successful login from IP 192.168.1.1',
        },
        {
          id: 2,
          action: 'Update Profile',
          timestamp: new Date('2023-04-19T14:45:00'),
          details: 'Changed role to Administrator',
        },
        {
          id: 3,
          action: 'Create Document',
          timestamp: new Date('2023-04-18T09:15:00'),
          details: 'Created new document "Project Proposal"',
        },
        {
          id: 4,
          action: 'Delete File',
          timestamp: new Date('2023-04-17T16:20:00'),
          details: 'Deleted file "old_report.pdf"',
        },
      ];
    }, 500);
  }

  updatePermission(permission: Permission): void {
    // Implement the logic to update permission
  }

  isAllSelected() {
    const numSelected = this.selection.selected.length;
    const numRows = this.dataSource.data.length;
    return numSelected === numRows;
  }

  masterToggle() {
    this.isAllSelected()
      ? this.selection.clear()
      : this.dataSource.data.forEach((row) => this.selection.select(row));

    this.dataSource.data = this.sortWithSelectedOnTop(this.dataSource.data);
    this.checkForChanges();
  }

  toggleSelection(row: Atm) {
    this.selection.toggle(row);
    this.dataSource.data = this.sortWithSelectedOnTop(this.dataSource.data);
    this.checkForChanges();
  }

  checkboxLabel(row?: Atm): string {
    if (!row) {
      return `${this.isAllSelected() ? 'select' : 'deselect'} all`;
    }
    return `${this.selection.isSelected(row) ? 'deselect' : 'select'} row ${
      row.atmId
    }`;
  }

  applyFilter(event: Event) {
    const filterValue = (event.target as HTMLInputElement).value;
    this.search = filterValue.trim().toLowerCase();
    this.currentPage = 1;
    this.loadATMS();
  }

  private sortWithSelectedOnTop(atms: Atm[]): Atm[] {
    return [...atms].sort((a, b) => {
      const aSelected = this.selection.isSelected(a);
      const bSelected = this.selection.isSelected(b);

      if (aSelected && !bSelected) return -1;
      if (!aSelected && bSelected) return 1;

      // If selection status is the same, sort by ATM ID
      return a.atmName.localeCompare(b.atmName);
    });
  }

  saveSelectedAtms(): void {
    // Get all ATMs from the data source
    const allAtms = this.dataSource.data;
    const currentSelection = new Set(
      this.selection.selected.map((atm) => atm.atmId)
    );

    // Filter only the ATMs that have changed status
    const changedAtms = allAtms.filter((atm) => {
      const wasInitiallySelected = this.initialSelection.has(atm.atmId);
      const isCurrentlySelected = currentSelection.has(atm.atmId);
      return wasInitiallySelected !== isCurrentlySelected; // Only include if status changed
    });

    // Create ATM accesses array only for changed terminals
    const atmAccesses: AtmAccess[] = changedAtms.map((atm) => ({
      terminalId: atm.atmId,
      userId: this.userId.toString(),
      status: currentSelection.has(atm.atmId) ? 'CHECKED' : 'UNCHECKED',
    }));

    if (atmAccesses.length > 0) {
      this.store.dispatch(saveAtmAccess({ atmAccesses }));
    } else {
    }
  }

  confirmSave(): void {
    const dialogRef = this.dialog.open(ConfirmDialogComponent, {
      width: '400px',
      data: {
        title: 'Confirm Save',
        message: 'Are you sure you want to save these ATM assignments?',
        confirmText: 'Save',
        cancelText: 'Cancel',
      },
    });

    dialogRef.afterClosed().subscribe((result) => {
      if (result) {
        this.saveSelectedAtms();
      }
    });
  }

  private checkForChanges(): void {
    const currentSelection = new Set(
      this.selection.selected.map((atm) => atm.atmId)
    );

    this.hasChanges =
      this.initialSelection.size !== currentSelection.size ||
      [...currentSelection].some((id) => !this.initialSelection.has(id)) ||
      [...this.initialSelection].some((id) => !currentSelection.has(id));
  }

  loadUserProfile(): void {
    if (!this.userId) return;

    this.dashboardService.getUserProfileOverview(this.userId).subscribe({
      next: (profile) => {
        this.userProfile = profile;
      },
    });
  }

  getInitials(domainId: string): string {
    if (!domainId) return 'NA';
    const parts = domainId.split('.');
    if (parts.length >= 2) {
      return (parts[0][0] + parts[1][0]).toUpperCase();
    }
    return domainId.substring(0, 2).toUpperCase();
  }

  private resetState(): void {
    this.selection.clear();
    this.initialSelection.clear();
    this.dataSource.data = [];
    this.currentPage = 1;
    this.pageSize = 10;
    this.search = '';
    this.status = '';
    this.sort = '';
    this.sortBy = 'atmName';
    this.problem = '';
  }

  private loadAtmAccess(): void {
    if (!this.userId) {
      return;
    }

    this.store.dispatch(
      AtmAccessActions.fetchAtmAccess({
        userId: this.userId.toString(),
      })
    );
  }

  private updateSelections(atmAccesses: AtmAccess[]): void {
    this.selection.clear();
    this.initialSelection.clear();

    const checkedAccesses = atmAccesses.filter(
      (access) => access.status === 'CHECKED'
    );
    const atmMap = new Map(this.dataSource.data.map((atm) => [atm.atmId, atm]));

    checkedAccesses.forEach((access) => {
      const atm = atmMap.get(access.terminalId);
      if (atm) {
        this.selection.select(atm);
        this.initialSelection.add(atm.atmId);
      }
    });

    // Sort the data with selected items on top
    this.dataSource.data = this.sortWithSelectedOnTop(this.dataSource.data);
    this.checkForChanges();
  }
}

// Create a new component for the confirmation dialog
@Component({
  selector: 'app-confirm-dialog',
  template: `
    <div class="p-6">
      <h2 class="text-xl font-semibold mb-4">{{ data.title }}</h2>
      <p class="text-gray-600 mb-6">{{ data.message }}</p>
      <div class="flex justify-end gap-4">
        <button mat-button (click)="dialogRef.close(false)">
          {{ data.cancelText }}
        </button>
        <button
          mat-raised-button
          color="primary"
          (click)="dialogRef.close(true)"
          class="bg-rose-600 hover:bg-rose-700"
        >
          {{ data.confirmText }}
        </button>
      </div>
    </div>
  `,
  standalone: true,
  imports: [MatButtonModule, MatDialogModule],
})
export class ConfirmDialogComponent {
  constructor(
    public dialogRef: MatDialogRef<ConfirmDialogComponent>,
    @Inject(MAT_DIALOG_DATA)
    public data: {
      title: string;
      message: string;
      confirmText: string;
      cancelText: string;
    }
  ) {}
}
