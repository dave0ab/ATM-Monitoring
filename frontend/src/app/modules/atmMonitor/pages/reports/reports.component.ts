import { CommonModule } from '@angular/common';
import { HttpClientModule } from '@angular/common/http';
import { Component, OnInit, OnDestroy, ViewChild } from '@angular/core';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatMenuModule } from '@angular/material/menu';
import { MatPaginator, MatPaginatorModule } from '@angular/material/paginator';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatSelectModule } from '@angular/material/select';
import { MatSort, MatSortModule } from '@angular/material/sort';
import { MatTableModule } from '@angular/material/table';
import { MatTableDataSource } from '@angular/material/table';
import { NgxDaterangepickerMd } from 'ngx-daterangepicker-material';
import { AlertErrorComponent } from 'src/app/modules/shared/components/alerts/alert-error/alert-error.component';
import { AlertWarningComponent } from 'src/app/modules/shared/components/alerts/alert-warning/alert-warning.component';
import { PrimaryComponent } from 'src/app/modules/shared/components/buttons/primary.component';
import { InputComponent } from 'src/app/modules/shared/components/inputs/input/input.component';
import { LoadingComponent } from 'src/app/modules/shared/components/loading/loading.component';
import { AtmCurrentStatus } from '../../interfaces/atm.interface';
import { Store } from '@ngrx/store';
import { Router } from '@angular/router';

import { Observable, Subject } from 'rxjs';
import { takeUntil, map, debounceTime } from 'rxjs/operators';
import {
  selectAtmDetailLoading,
  selectReportStatus,
  selectReportStatusError,
  selectReportStatusLoading,
  selectExcelExportLoading,
  selectExcelExportError,
} from '../../store/atm/atm.selectors';
import {
  fetchStatusReports,
  exportAtmExcel,
} from '../../store/atm/atm.actions';
import {
  FilterByStatusPipe,
  ArrayLengthPipe,
} from '../../pipes/filter-by-status.pipe';

@Component({
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
    LoadingComponent,
    PrimaryComponent,
    InputComponent,
    MatProgressBarModule,
    HttpClientModule,
    AlertWarningComponent,
    AlertErrorComponent,
    FilterByStatusPipe,
    ArrayLengthPipe,
  ],
  selector: 'app-atm-dashboard',
  templateUrl: './reports.component.html',
})
export class ReportsComponent implements OnInit, OnDestroy {
  atms: AtmCurrentStatus[] = [];
  currentStatus$: Observable<AtmCurrentStatus[] | null>;
  loading$: Observable<boolean>;
  error$: Observable<string | null>;

  filteredAtms: AtmCurrentStatus[] = [];
  statusFilter: string = '';
  searchTerm: string = '';

  private destroy$ = new Subject<void>();

  @ViewChild(MatPaginator) paginator!: MatPaginator;
  @ViewChild(MatSort) sort!: MatSort;
  displayedColumns: string[] = ['terminalId', 'atmName', 'status'];
  dataSource: MatTableDataSource<AtmCurrentStatus>;

  currentDate = new Date();
  excelLoading$ = this.store.select(selectExcelExportLoading);
  excelError$ = this.store.select(selectExcelExportError);

  constructor(private store: Store, private router: Router) {
    this.currentStatus$ = this.store.select(selectReportStatus) as Observable<
      AtmCurrentStatus[] | null
    >;
    this.loading$ = this.store.select(selectReportStatusLoading);
    this.error$ = this.store.select(selectReportStatusError);
    this.dataSource = new MatTableDataSource<AtmCurrentStatus>([]);
  }

  ngOnInit() {
    this.initializeData();
  }

  ngOnDestroy() {
    this.destroy$.next();
    this.destroy$.complete();
  }

  ngAfterViewInit() {
    this.dataSource.paginator = this.paginator;
    this.dataSource.sort = this.sort;
  }

  private initializeData() {
    // Dispatch the action to fetch current status
    this.store.dispatch(fetchStatusReports());

    // Subscribe to the current status updates
    this.currentStatus$
      .pipe(
        takeUntil(this.destroy$),
        map((status) => {
          if (status) {
            this.atms = status;
            this.applyFilters(); // Apply any active filters
          }
        })
      )
      .subscribe();
  }

  applyFilters() {
    const filteredAtms = this.atms.filter((atm) => {
      const matchesStatus =
        !this.statusFilter || atm.status === this.statusFilter;
      const matchesSearch =
        !this.searchTerm ||
        atm.atmName.toLowerCase().includes(this.searchTerm.toLowerCase()) ||
        atm.terminalId.toLowerCase().includes(this.searchTerm.toLowerCase());

      return matchesStatus && matchesSearch;
    });
    this.dataSource.data = filteredAtms;
  }

  exportCSV() {
    const headers = ['Terminal ID', 'ATM Name', 'Status', 'ID'];
    const csvContent = [
      headers.join(','),
      ...this.filteredAtms.map(
        (atm) => `${atm.terminalId},${atm.atmName},${atm.status},${atm.id}`
      ),
    ].join('\n');

    const blob = new Blob([csvContent], { type: 'text/csv;charset=utf-8;' });
    const link = document.createElement('a');
    if (link.download !== undefined) {
      const url = URL.createObjectURL(blob);
      link.setAttribute('href', url);
      link.setAttribute('download', 'atm_status_report.csv');
      link.style.visibility = 'hidden';
      document.body.appendChild(link);
      link.click();
      document.body.removeChild(link);
    }
  }

  exportPDF() {
    // Implement PDF export logic
    console.log('Exporting PDF... (Implement actual PDF export logic)');
  }
}
