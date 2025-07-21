import { Component, ViewChild } from '@angular/core';
import { CommonModule, DatePipe } from '@angular/common';
import { HttpClientModule } from '@angular/common/http';
import { FormBuilder, FormsModule, ReactiveFormsModule } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatCheckboxModule } from '@angular/material/checkbox';
import {
  MatDialog,
  MatDialogModule,
  MatDialogRef,
} from '@angular/material/dialog';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatMenuModule } from '@angular/material/menu';
import {
  MatPaginator,
  MatPaginatorModule,
  PageEvent,
} from '@angular/material/paginator';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSelectModule } from '@angular/material/select';
import { MatSortModule } from '@angular/material/sort';
import { MatTableDataSource, MatTableModule } from '@angular/material/table';
import { NgxDaterangepickerMd } from 'ngx-daterangepicker-material';
import { AlertErrorComponent } from 'src/app/modules/shared/components/alerts/alert-error/alert-error.component';
import { AlertWarningComponent } from 'src/app/modules/shared/components/alerts/alert-warning/alert-warning.component';
import { PrimaryComponent } from 'src/app/modules/shared/components/buttons/primary.component';
import { InputComponent } from 'src/app/modules/shared/components/inputs/input/input.component';
import { LoadingComponent } from 'src/app/modules/shared/components/loading/loading.component';
import { SelectionModel } from '@angular/cdk/collections';
import { DomSanitizer, SafeResourceUrl } from '@angular/platform-browser';
import { Router, ActivatedRoute } from '@angular/router';
import { Store } from '@ngrx/store';
import { Observable, Subject, takeUntil, filter } from 'rxjs';
import { AtmAccess } from 'src/app/modules/atmMonitor/interfaces/atm-access.interface';
import {
  Atm,
  FetchAtmListState,
} from 'src/app/modules/atmMonitor/interfaces/atm.interface';
import { LoadingService } from 'src/app/modules/atmMonitor/services/loading.service';
import { ToastService } from 'src/app/modules/atmMonitor/services/toast.service';
import { saveAtmAccess } from 'src/app/modules/atmMonitor/store/access/access.actions';
import { selectAtmAccesses } from 'src/app/modules/atmMonitor/store/access/access.selectors';
import { fetchAtm } from 'src/app/modules/atmMonitor/store/atm/atm.actions';
import {
  selectAtmState,
  selectAtmLoading,
  selectAtmTotalItems,
  selectAtmError,
} from 'src/app/modules/atmMonitor/store/atm/atm.selectors';
import { LocationDialogComponent } from 'src/app/modules/atmMonitor/components/location-dialog/location-dialog.component';
import { ConfirmDialogComponent } from 'src/app/modules/atmMonitor/pages/user-details/user-details.component';
import { selectLocationState } from 'src/app/modules/atmMonitor/store/location/location.selectors';
import { updateLocation } from 'src/app/modules/atmMonitor/store/location/location.actions';
import { MatSnackBar } from '@angular/material/snack-bar';

interface AtmLocation {
  atmId: string;
  latitude: number;
  longitude: number;
}

@Component({
  selector: 'app-atm-location-management',
  standalone: true,
  imports: [
    CommonModule,
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
    AlertErrorComponent,
    MatProgressBarModule,
    HttpClientModule,
    AlertErrorComponent,
    FormsModule,
    MatDialogModule,
    MatProgressSpinnerModule,
  ],
  providers: [DatePipe],
  templateUrl: './atm-location-management.component.html',
  styleUrls: ['./atm-location-management.component.scss'],
})
export class AtmLocationManagementComponent {
  onHeaderClick(column: string): void {
    // Implement sorting logic here if needed
  }

  private dialogRef: MatDialogRef<LocationDialogComponent> | null = null;
  private isProcessingClick = false;

  onRowClick(atm: Atm, event: MouseEvent): void {
    event.preventDefault();
    event.stopPropagation();

    if (this.isProcessingClick) {
      return;
    }

    this.isProcessingClick = true;

    if (this.dialogRef) {
      this.dialogRef.close();
      this.dialogRef = null;
      this.isProcessingClick = false;
      return;
    }

    this.dialogRef = this.dialog.open(LocationDialogComponent, {
      width: '600px',
      data: {
        atmId: atm.atmId,
        atmName: atm.atmName,
        mapLink: atm.mapLink,
      },
      disableClose: false,
    });

    this.dialogRef.afterClosed().subscribe(() => {
      this.dialogRef = null;
      this.isProcessingClick = false;
    });

    setTimeout(() => {
      this.isProcessingClick = false;
    }, 500);
  }

  onPageChange(event: PageEvent): void {
    this.currentPage = event.pageIndex + 1;
    this.pageSize = event.pageSize;
    this.loadATMS();
  }
  userId: number = 0;

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
  displayedColumns: string[] = ['atmId', 'atmName', 'actions'];
  private destroy$ = new Subject<void>();
  atms: Atm[] = [];

  hasChanges: boolean = false;

  initialSelection: Set<string> = new Set();

  selectedAtm: Atm | null = null;

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
    private snackBar: MatSnackBar
  ) {
    this.fetch$ = this.store.select(selectAtmState);
    this.loading$ = this.store.select(selectAtmLoading);
    this.totalItems$ = this.store.select(selectAtmTotalItems);

    // Handle location update success/failure
    this.store.select(selectLocationState).subscribe((state) => {
      if (state.error) {
        this.snackBar.open('Failed to update location', 'Close', {
          duration: 3000,
          horizontalPosition: 'right',
          verticalPosition: 'top',
          panelClass: ['error-snackbar'],
        });
      }
      if (!state.loading && !state.error && state.location) {
        this.snackBar.open('Location updated successfully', 'Close', {
          duration: 3000,
          horizontalPosition: 'right',
          verticalPosition: 'top',
          panelClass: ['success-snackbar'],
        });
        this.dialogRef?.close();
        this.loadATMS();
      }
    });
  }
  @ViewChild(MatPaginator) paginator!: MatPaginator;

  ngOnInit(): void {
    this.route.params.subscribe((params) => {
      this.userId = +params['id'];
      this.loadATMS();
    });

    this.fetch$
      .pipe(
        takeUntil(this.destroy$),
        filter((response): response is FetchAtmListState => !!response?.atm)
      )
      .subscribe((response) => {
        const atms = response.atm?.content || [];
        this.atms = atms;
        this.dataSource.data = atms;
      });

    this.store
      .select(selectAtmAccesses)
      .pipe(
        takeUntil(this.destroy$),
        filter((atmAccesses) => !!this.dataSource.data.length)
      )
      .subscribe((atmAccesses) => {
        this.selection.clear();
        this.initialSelection.clear();

        if (atmAccesses && atmAccesses.length > 0) {
          atmAccesses.forEach((access) => {
            const atm = this.dataSource.data.find(
              (a) => a.atmId === access.terminalId
            );
            if (atm && access.status === 'CHECKED') {
              this.selection.select(atm);
              this.initialSelection.add(atm.atmId);
            }
          });
        }

        this.dataSource.data = this.sortWithSelectedOnTop(this.dataSource.data);
        this.checkForChanges();
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
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  loadATMS() {
    this.store.dispatch(
      fetchAtm({
        pageNumber: this.currentPage - 1,
        pageSize: this.pageSize,
        status: this.status || '',
        search: this.search || '',
        sort: this.sort || '',
        sortBy: this.sortBy || '',
        problem: this.problem || '',
      })
    );
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
      // First sort by selection status
      const aSelected = this.selection.isSelected(a);
      const bSelected = this.selection.isSelected(b);

      if (aSelected && !bSelected) return -1;
      if (!aSelected && bSelected) return 1;

      // If still equal, sort by ATM ID
      return a.atmId.localeCompare(b.atmId);
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

  getSafeMapUrl(mapsLink: string | undefined): SafeResourceUrl {
    if (!mapsLink) {
      return this.sanitizer.bypassSecurityTrustResourceUrl('');
    }
    return this.sanitizer.bypassSecurityTrustResourceUrl(mapsLink);
  }

  updateLocation(atmId: string, latitude: string, longitude: string) {
    this.store.dispatch(
      updateLocation({
        location: {
          tid: atmId,
          la: latitude,
          lo: longitude,
        },
      })
    );
  }
}
