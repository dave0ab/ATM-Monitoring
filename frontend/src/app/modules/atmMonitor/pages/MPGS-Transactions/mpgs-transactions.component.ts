import { Component, OnInit, OnDestroy, ViewChild } from '@angular/core';
import { CommonModule, DatePipe } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { MatPaginator, PageEvent } from '@angular/material/paginator';
import { MatSort } from '@angular/material/sort';
import { MatTableDataSource } from '@angular/material/table';
import { Store } from '@ngrx/store';
import { Observable, Subject, takeUntil, map, interval } from 'rxjs';
import { MPGSTransaction } from '../../../dashboard/interfaces/Dashboard.intreface';
import * as DashboardActions from '../../../dashboard/store/dashboard.actions';
import {
  selectMPGSTransactions,
  selectMPGSTransactionsLoading,
  selectMPGSTransactionsError,
} from '../../../dashboard/store/dashboard.selectors';
import { MatTableModule } from '@angular/material/table';
import { MatPaginatorModule } from '@angular/material/paginator';
import { MatSortModule } from '@angular/material/sort';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { HttpClient } from '@angular/common/http';

import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { environment } from 'src/environments/environment';
import { TransactionDetailsModalComponent } from './transaction-details';
import * as dayjs from 'dayjs';
import {
  DaterangepickerComponent,
  DaterangepickerDirective,
  NgxDaterangepickerMd,
} from 'ngx-daterangepicker-material';

@Component({
  selector: 'app-mpgs-transactions',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    MatTableModule,
    MatPaginatorModule,
    MatSortModule,
    MatProgressBarModule,
    MatDialogModule,
    NgxDaterangepickerMd,
  ],
  providers: [DatePipe],
  templateUrl: './mpgs-transactions.component.html',
  styleUrls: ['./mpgs-transactions.component.scss'],
})
export class MPGSTransactionsComponent implements OnInit, OnDestroy {
  @ViewChild(MatPaginator) paginator!: MatPaginator;
  @ViewChild(MatSort) matSort!: MatSort;
  @ViewChild(DaterangepickerDirective)
  pickerDirective!: DaterangepickerDirective;

  // Table data
  dataSource: MatTableDataSource<MPGSTransaction> =
    new MatTableDataSource<MPGSTransaction>();

  picker!: DaterangepickerComponent;

  selectedDate!: { startDate: dayjs.Dayjs; endDate: dayjs.Dayjs };
  startDate: string = '';
  endDate: string = '';
  date: string = '';

  displayedColumns: string[] = [
    'orderId',
    'merchant',
    'merchantName',
    'transactionType',
    'timeOfRecord',
    'cardNumber',
    'amount',
    // 'gatewayCode',
    'cardholderName',
    'brand',
    'orderStatus',
    'message',
  ];

  // Filter options
  statusOptions: string[] = [
    'CAPTURED',
    'AUTHENTICATION INITIATED',
    'AUTHENTICATION UNSUCCESSFUL',
    'AUTHENTICATED',
    'FAILED',
    'AUTHORIZED',
  ];
  brandOptions: string[] = ['VISA', 'MASTERCARD'];

  // State
  orderStatus: string = '';
  search: string = '';
  brand: string = '';
  currentPage: number = 1;
  pageSize: number = 10;

  // Observables
  loading$: Observable<boolean>;
  error$: Observable<string | null>;
  transactions$: Observable<any>;
  totalItems$: Observable<number>;

  private destroy$ = new Subject<void>();

  constructor(
    private store: Store,
    private datePipe: DatePipe,
    private http: HttpClient,
    private dialog: MatDialog
  ) {
    this.loading$ = this.store.select(selectMPGSTransactionsLoading);
    this.error$ = this.store.select(selectMPGSTransactionsError);
    this.transactions$ = this.store.select(selectMPGSTransactions);
    this.totalItems$ = this.store
      .select(selectMPGSTransactions)
      .pipe(map((response) => response?.totalElements || 0));
  }

  ngOnInit(): void {
    this.fetchMPGSTransactions();

    // Set up auto-refresh every minute
    interval(60000) // 60000 ms = 1 minute
      .pipe(takeUntil(this.destroy$))
      .subscribe(() => {
        this.fetchMPGSTransactions();
      });

    this.transactions$
      .pipe(takeUntil(this.destroy$))
      .subscribe((transactions) => {
        if (transactions) {
          this.dataSource.data = transactions.content;
        }
      });

    this.loading$.pipe(takeUntil(this.destroy$)).subscribe((loading) => {});

    this.error$.pipe(takeUntil(this.destroy$)).subscribe((error) => {
      if (error) {
      }
    });
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  fetchMPGSTransactions(): void {
    this.store.dispatch(
      DashboardActions.fetchMPGSTransactions({
        page: this.currentPage - 1,
        size: this.pageSize,
        orderStatus: this.orderStatus,
        search: this.search,
        brand: this.brand,
        startDate: this.startDate,
        endDate: this.endDate,
      })
    );
  }

  onPageChange(event: PageEvent): void {
    this.currentPage = event.pageIndex + 1;
    this.pageSize = event.pageSize;
    this.fetchMPGSTransactions();
  }

  onHeaderClick(columnName: string): void {
    this.fetchMPGSTransactions();
  }

  onStatusChange(event: Event): void {
    const target = event.target as HTMLSelectElement;
    this.orderStatus = target.value;
    this.fetchMPGSTransactions();
  }

  onbBrandChange(event: Event) {
    const target = event.target as HTMLSelectElement;
    this.brand = target.value;
    this.fetchMPGSTransactions();
  }
  onSearchChange(event: Event): void {
    const target = event.target as HTMLInputElement;
    this.search = target.value;
    this.fetchMPGSTransactions();
  }

  onInputChangeDateFilter(event: any): void {
    if (event?.startDate && event?.endDate) {
      this.startDate = event.startDate.format('YYYY-MM-DD');
      this.endDate = event.endDate.format('YYYY-MM-DD');
      this.date = `${this.startDate} To ${this.endDate}`;
      this.fetchMPGSTransactions();
    }
  }

  onClearRange(): void {
    this.startDate = '';
    this.endDate = '';
    this.date = '';
    this.selectedDate = {} as any;
    this.fetchMPGSTransactions();
  }

  clearFilters(): void {
    this.orderStatus = '';
    this.brand = '';
    this.search = '';
    this.currentPage = 1;
    this.startDate = '';
    this.endDate = '';
    this.date = '';
    this.selectedDate = {} as any;

    // Clear the date picker
    if (this.pickerDirective) {
      this.pickerDirective.clear();
    }

    if (this.paginator) {
      this.paginator.pageIndex = 0;
    }

    this.fetchMPGSTransactions();
  }

  formatDate(dateString: string | undefined): string | null {
    if (!dateString) return 'N/A';
    return this.datePipe.transform(dateString, 'MMM dd, yyyy hh:mm:ss a');
  }

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

  getStatusClass(status: string, column?: string): string {
    if (!status) return 'bg-gray-100 text-gray-800';

    const upperStatus = status.toUpperCase();

    // Handle message column
    if (column === 'message') {
      return status === 'Approved'
        ? 'bg-green-100 text-green-800'
        : 'bg-rose-100 text-rose-800';
    }

    // Handle order status column
    if (upperStatus === 'CAPTURED') {
      return 'bg-green-100 text-green-800';
    }
    if (upperStatus === 'AUTHENTICATED') {
      return 'bg-green-100 text-green-800';
    }
    if (
      status
        .normalize('NFKC') // Normalize Unicode chars (e.g. non-breaking spaces)
        .replace(/\s+/g, '_') // Replace ANY whitespace with `_`
        .replace(/_+/g, '_') // Collapse multiple underscores into one
        .trim() // Trim leading/trailing underscores
        .toUpperCase() === // Make comparison case-insensitive
      'AUTHENTICATION_INITIATED'
    ) {
      return 'bg-yellow-100 text-yellow-800';
    }
    return 'bg-rose-100 text-rose-800';
  }

  getBrandClass(brand: string): string {
    switch (brand?.toUpperCase()) {
      case 'VISA':
        return 'bg-blue-100 text-blue-800';
      case 'MASTERCARD':
        return 'bg-orange-100 text-orange-800';
      case 'AMEX':
        return 'bg-green-100 text-green-800';
      default:
        return 'bg-gray-100 text-gray-800';
    }
  }

  getMPGSTransactionDetails(orderId: string): Observable<MPGSTransaction[]> {
    return this.http.get<MPGSTransaction[]>(
      `${environment.apiUrl}/MPGS_Transactions/${orderId}`
    );
  }

  onRowClick(transaction: MPGSTransaction): void {
    this.dialog.open(TransactionDetailsModalComponent, {
      width: '80%',
      maxHeight: '90vh',
      data: { orderId: transaction.orderId },
    });
  }
}
