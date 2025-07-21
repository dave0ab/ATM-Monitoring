// atm-list.component.ts
import {
  Component,
  EventEmitter,
  Input,
  OnInit,
  Output,
  ViewChild,
} from '@angular/core';
import { CommonModule, DatePipe } from '@angular/common';
import { FormBuilder, FormsModule, ReactiveFormsModule } from '@angular/forms';
import { MatTableDataSource, MatTableModule } from '@angular/material/table';
import {
  MatPaginatorModule,
  MatPaginator,
  PageEvent,
} from '@angular/material/paginator';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatMenuModule } from '@angular/material/menu';
import { NgxDaterangepickerMd } from 'ngx-daterangepicker-material';
import { SelectionModel } from '@angular/cdk/collections';
import {
  Atm,
  AtmResponse,
  FetchAtmListState,
} from '../../interfaces/atm.interface';
import { Observable, Subject, Subscription, takeUntil, finalize } from 'rxjs';
import { ActivatedRoute, Router } from '@angular/router';
import { ToastService } from '../../services/toast.service';
import { LoadingService } from '../../services/loading.service';
import { Store } from '@ngrx/store';
import {
  selectAtmError,
  selectAtmLoading,
  selectAtmState,
  selectAtmTotalItems,
  selectExcelExportLoading,
} from '../../store/atm/atm.selectors';
import {
  exportAtmExcel,
  fetchAtm,
  fetchGatewayMonitor,
} from '../../store/atm/atm.actions';
import { AlertErrorComponent } from 'src/app/modules/shared/components/alerts/alert-error/alert-error.component';
import { LoadingComponent } from 'src/app/modules/shared/components/loading/loading.component';
import { HttpClientModule } from '@angular/common/http';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { AlertWarningComponent } from 'src/app/modules/shared/components/alerts/alert-warning/alert-warning.component';
import { PrimaryComponent } from 'src/app/modules/shared/components/buttons/primary.component';
import { InputComponent } from 'src/app/modules/shared/components/inputs/input/input.component';
import { WebSocketService } from '../../services/web-socket.service';
import { DomSanitizer, SafeResourceUrl } from '@angular/platform-browser';
import { saveAtmAccess } from '../../store/access/access.actions';
import { AtmAccess } from '../../interfaces/atm-access.interface';
import { loadTips } from '../../store/navmenus/navmenus.actions';
import {
  selectTipsError,
  selectTipsLoading,
} from '../../store/navmenus/navmenus.selectors';
import { AtmService } from '../../services/atm.service';
import { MatSnackBar } from '@angular/material/snack-bar';
import { MatSnackBarModule } from '@angular/material/snack-bar';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { AuthService } from 'src/app/modules/auth/services/auth.service';

@Component({
  selector: 'app-atm-list',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    ReactiveFormsModule,
    MatTableModule,
    MatPaginatorModule,
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
    MatSnackBarModule,
    MatProgressSpinnerModule,
  ],
  templateUrl: './atm-list.component.html',
  styles: [
    `
      :host {
        display: block;
      }
    `,
  ],
  providers: [DatePipe],
})
export class AtmListComponent implements OnInit {
  safeMapUrl: SafeResourceUrl | null = null;
  sort!: string;
  sortBy!: string;
  problem!: string;
  atmAccesses: AtmAccess[] = [];
  isExporting: boolean = false;
  excelLoading$ = this.store.select(selectExcelExportLoading);
  private intervalId: any;
  isAdminOrIT!: boolean;

  getSafeMapUrl(mapsLink: string | undefined): SafeResourceUrl {
    return this.sanitizer.bypassSecurityTrustResourceUrl(mapsLink!);
  }
  dataSource: MatTableDataSource<Atm> = new MatTableDataSource<Atm>();
  selection = new SelectionModel<Atm>(true, []);
  private atmUpdatesSubscription!: Subscription;
  atms: Atm[] | undefined;
  @Input() showModal: boolean = false;
  editAtm(_t129: any) {
    throw new Error('Method not implemented.');
  }
  public error$: Observable<string | null> = this.store.select(selectAtmError);

  onPageChange(event: any): void {
    this.currentPage = event.pageIndex + 1;
    this.pageSize = event.pageSize;
    this.loadATMS();
  }

  // ... existing code ...

  formatAmount(amount: number | string): string {
    if (!amount) return '0';
    // Convert to number and format with thousand separators
    const numAmount = typeof amount === 'string' ? parseFloat(amount) : amount;
    return numAmount.toLocaleString('en-US', {
      minimumFractionDigits: 2,
      maximumFractionDigits: 2,
    });
  }

  formatStatusText(status: string): string {
    if (!status) return '';
    // Replace underscores with spaces and add line breaks
    return status
      .replace(/_/g, ' ')
      .replace('AUTHENTICATION', 'AUTHENTICATION\n')
      .replace('UNSUCCESSFUL', 'UNSUCCESSFUL');
  }

  // ... existing code ...

  openDeleteModal(_t133: any) {
    throw new Error('Method not implemented.');
  }
  changeStatus(_t133: any) {
    throw new Error('Method not implemented.');
  }
  editForeclosure(_t133: any) {
    throw new Error('Method not implemented.');
  }
  checkboxLabel(row?: Atm): string {
    if (!row) {
      return `${this.isAllSelected() ? 'select' : 'deselect'} all`;
    }
    return `${this.selection.isSelected(row) ? 'deselect' : 'select'} row ${
      row.atmId + 1
    }`;
  }

  checkboxLabell(row?: Atm): string {
    if (!row) {
      return `${this.isAllSelected() ? 'select' : 'deselect'} all`;
    }
    return `${this.selection.isSelected(row) ? 'deselect' : 'select'} row ${
      row.atmId + 1
    }`;
  }
  clearFilters() {
    // Reset all filter values
    this.selectedProblem = '';
    this.problem = '';
    this.search = '';
    this.status = '';
    this.sort = '';
    this.sortBy = '';
    this.selectedStatus = '';
    this.selectedDateRange = null;
    this.currentPage = 1;

    // Reset paginator if it exists
    if (this.paginator) {
      this.paginator.pageIndex = 0;
    }

    // Clear URL parameters
    this.router.navigate([], {
      relativeTo: this.route,
      queryParams: {},
      queryParamsHandling: '',
    });

    // Reload the ATMs with cleared filters
    this.loadATMS();
  }

  fetch$: Observable<FetchAtmListState>;
  loading$: Observable<boolean>;
  loading: boolean = true;
  totalItems$: Observable<number | null>;
  isStopedLoading = true;
  currentPage: number = 1;
  pageSize: number = 10;
  search: string = '';
  status: string = '';
  count: number = 0;
  tips$ = this.store.select(selectAllTips);
  loadingg$ = this.store.select(selectTipsLoading);
  errorr$ = this.store.select(selectTipsError);

  private destroy$ = new Subject<void>();
  constructor(
    private store: Store,
    private router: Router,
    private _formBuilder: FormBuilder,
    private toastService: ToastService,
    private loadingService: LoadingService,
    private WebSocketService: WebSocketService,
    private route: ActivatedRoute,
    private datePipe: DatePipe,
    private sanitizer: DomSanitizer,
    private atmService: AtmService,
    private snackBar: MatSnackBar,
    private authService: AuthService
  ) {
    this.fetch$ = this.store.select(selectAtmState);
    this.loading$ = this.store.select(selectAtmLoading);
    this.totalItems$ = this.store.select(selectAtmTotalItems);
  }

  displayedColumns: string[] = [
    // 'select',
    'atmId',
    'atmName',
    // 'atmIp',
    'actualOpStateDesc',
    'commStateDesc',
    'lastTransaction',
    'problem',
    'pingStatus',
    'cashLeft',
    //'actions',
  ];

  @ViewChild(MatPaginator) paginator!: MatPaginator;

  selectedExportType: string = '';

  exportExcel() {
    if (!this.selectedExportType || this.isExporting) return;

    this.isExporting = true;

    this.store.dispatch(
      exportAtmExcel({ exportType: this.selectedExportType })
    );

    // Subscribe to the loading state
    this.excelLoading$
      .pipe(
        takeUntil(this.destroy$),
        finalize(() => {
          // Only reset the exporting state when the download URL is received
          this.isExporting = false;
        })
      )
      .subscribe((isLoading) => {
        if (!isLoading) {
          // The download URL has been received and the download has started
          this.isExporting = false;
        }
      });
  }

  statuses = ['Active', 'Inactive', 'Under Maintenance'];
  selectedStatus = '';
  selectedDateRange: { startDate: Date; endDate: Date } | null = null;
  searchTerm = '';
  includeTrailing = false;
  error: string | null = null;
  selectedProblem: string = '';

  problemOptions: string[] = [
    'Cash Dispenser',
    'Encryptor',
    'Magnetic Card Reader',
    'Other',
  ];

  operationalStatuses: string[] = [
    'Online',
    'Offline',
    'Open',
    'Closed',
    'Downloading',
  ];

  isRemarkChanged = false;
  originalRemark: string = '';
  currentRemark: string = '';

  remarkChanged(newValue: string) {
    this.currentRemark = newValue;
    this.isRemarkChanged = newValue !== this.originalRemark;
  }
  convertToDate(timestamp: string | null | undefined): Date | null {
    if (!timestamp) {
      return null;
    }

    try {
      const year = '20' + timestamp.substring(0, 2); // Get the full year, assuming the format is YY
      const month = parseInt(timestamp.substring(2, 4), 10) - 1; // Convert MM to zero-based index
      const day = parseInt(timestamp.substring(4, 6), 10); // Day (DD)
      const hours = parseInt(timestamp.substring(7, 9), 10); // Hours (HH)
      const minutes = parseInt(timestamp.substring(10, 12), 10); // Minutes (MM)
      const seconds = parseInt(timestamp.substring(13, 15), 10); // Seconds (SS)

      return new Date(parseInt(year), month, day, hours, minutes, seconds);
    } catch (error) {
      return null;
    }
  }

  saveRemark() {
    if (!this.selectedAtm?.atmId) {
      return;
    }

    this.atmService
      .updateRemark(this.selectedAtm.atmId, this.currentRemark)
      .subscribe({
        next: (response) => {
          this.originalRemark = this.currentRemark;
          this.selectedAtm!.remark = this.currentRemark;
          this.isRemarkChanged = false;
          this.snackBar.open('Remark saved successfully', 'Close', {
            duration: 3000,
            horizontalPosition: 'end',
            verticalPosition: 'top',
            panelClass: ['success-snackbar'],
          });

          this.loadATMS();
        },
        error: (error) => {
          this.snackBar.open('Failed to save remark', 'Close', {
            duration: 3000,
            horizontalPosition: 'end',
            verticalPosition: 'top',
            panelClass: ['error-snackbar'],
          });
        },
      });
  }

  ngOnInit() {
    this.store.dispatch(loadTips());
    this.route.queryParams.subscribe((params) => {
      this.search = params['search'] || ''; // Get search parameter
      this.loadATMS(); // Load ATMs with the updated search term
    });

    this.isAdminOrIT = this.authService.isAdminOrIT();

    this.loading$.pipe(takeUntil(this.destroy$)).subscribe((value) => {
      this.isStopedLoading = value;
    });

    this.fetch$.pipe(takeUntil(this.destroy$)).subscribe((response) => {
      this.atms = response.atm?.content;
      this.dataSource.data = response.atm?.content || [];
    });

    this.totalItems$.pipe(takeUntil(this.destroy$)).subscribe((response) => {
      this.count = response || 0;
    });

    this.WebSocketService.getAtmUpdates()
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (atmUpdate) => {
          this.loadATMS();
          this.updateTableWithNewData(atmUpdate);
        },
        error: (err) => console.error('WebSocket error:', err),
      });

    this.intervalId = setInterval(() => {
      this.loadATMS();
    }, 60000);

    this.store.dispatch(loadTips());
  }

  formatTransactionDate(dateString: string | undefined): string | null {
    if (!dateString) return 'N/A';

    try {
      // Input format: "241203 10:00:48" (YYMMDD HH:mm:ss)
      // First, clean up the input by ensuring proper spacing
      const cleanDateString = dateString.replace(/\s+/g, ' ').trim();

      // Split into date and time parts
      const datePart = cleanDateString.slice(0, 6);
      const timePart = cleanDateString.slice(6).trim();

      const year = parseInt(`20${datePart.slice(0, 2)}`);
      const month = parseInt(datePart.slice(2, 4)) - 1; // Months are 0-based
      const day = parseInt(datePart.slice(4, 6));

      // Parse time
      const [hours, minutes, seconds] = timePart.split(':').map(Number);

      const date = new Date(year, month, day, hours, minutes, seconds);

      // Validate the date
      if (isNaN(date.getTime())) {
        return 'Invalid Date';
      }

      return this.datePipe.transform(date, 'MMM dd, yyyy hh:mm:ss a');
    } catch (error) {
      return dateString; // Return original string if formatting fails
    }
  }

  ngOnDestroy(): void {
    // Unsubscribe to avoid memory leaks
    if (this.atmUpdatesSubscription) {
      this.atmUpdatesSubscription.unsubscribe();
    }
    if (this.intervalId) {
      clearInterval(this.intervalId);
    }
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

    this.store.dispatch(saveAtmAccess({ atmAccesses: this.atmAccesses }));
  }

  onHeaderClick(columnName: string): void {
    this.sort = 'true'; // Enable sorting each time sortingDataAccessor is used
    this.sortBy = columnName; // Enable

    this.loadATMS();
  }

  masterToggle() {
    this.isAllSelected()
      ? this.selection.clear()
      : this.dataSource.data.forEach((row) => this.selection.select(row));
  }

  isAllSelected() {
    const numSelected = this.selection.selected.length;
    const numRows = this.dataSource.data.length;
    return numSelected === numRows;
  }

  updateTableWithNewData(atmUpdate: Atm) {
    // Create a new array to hold updated ATM data
    const updatedData = this.dataSource.data.map((atm) => {
      if (atm.atmId === atmUpdate.atmId) {
        // If we find a matching ATM, return the updated object
        return { ...atm, ...atmUpdate };
      }
      // Otherwise, return the existing ATM object
      return atm;
    });

    // If the ATM update did not exist in the previous data, we add it
    if (!this.dataSource.data.some((atm) => atm.atmId === atmUpdate.atmId)) {
      updatedData.push(atmUpdate);
    }

    this.dataSource.data = updatedData;
    this.dataSource._updateChangeSubscription();
  }

  // for modal class

  @Output() closes = new EventEmitter<void>();

  selectedAtm: Atm | null = null;

  // Define the details for each section
  basicDetails = [
    { label: 'ATM ID', key: 'atmId' },
    { label: 'ATM Name', key: 'atmName' },
    { label: 'Model Description', key: 'modelDesc' },
    { label: 'Branch Name', key: 'branchName' },
    { label: 'Address', key: 'address1' },
    { label: 'ATM IP', key: 'atmIp' },
  ];

  operationalStatus = [
    { label: 'Required Operational State', key: 'requiredOpState' },
    { label: 'Actual Operational State', key: 'actualOpState' },
    { label: 'Actual Operational State Description', key: 'actualOpStateDesc' },
    { label: 'Status', key: 'status' },
    { label: 'State Name', key: 'stateName' },
  ];

  communication = [
    { label: 'Communication State', key: 'commsState' },
    { label: 'Communication State Description', key: 'commStateDesc' },
    { label: 'Last Updated', key: 'lastUpdated' },
    { label: 'Last Updated (Epoch)', key: 'lastUpdatedEpoch' },
  ];

  devicesAndFailures = [
    { label: 'Primary Device', key: 'deviceName' },
    { label: 'Primary Device Failure', key: 'failureName' },
    { label: 'Secondary Device', key: 'device2Name' },
    { label: 'Secondary Device Failure', key: 'failure2Name' },
  ];

  onAtmSelect(event: Event): void {
    const selectedAtmId = (event.target as HTMLSelectElement).value;
    this.selectedAtm =
      this.atms?.find((atm) => atm.atmId === selectedAtmId) || null;
  }

  onRowClick(atm: any): void {
    this.selectedAtm = { ...atm };
    this.originalRemark = atm.remark || '';
    this.currentRemark = atm.remark || '';
    this.isRemarkChanged = false;
    this.showModal = true;
  }

  toggleModal() {
    this.showModal = !this.showModal;
  }

  close() {
    this.closes.emit();
    this.toggleModal();
  }

  onProblemChange(event: any): void {
    this.problem = event.target.value;

    this.loadATMS();
  }

  onStatusChange(event: any): void {
    this.status = event.target.value;
    this.loadATMS();
  }

  onOperationalStatusChange(event: any): void {
    this.status = event.target.value;
    this.loadATMS();
  }
}
function selectAllTips(state: object): unknown {
  throw new Error('Function not implemented.');
}
