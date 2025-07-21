import { Component, OnInit, Inject, OnDestroy } from '@angular/core';
import {
  MAT_DIALOG_DATA,
  MatDialogRef,
  MatDialogModule,
} from '@angular/material/dialog';
import { Store } from '@ngrx/store';
import { Observable, Subject, takeUntil, tap, map } from 'rxjs';
import { MPGSTransaction } from '../../../dashboard/interfaces/Dashboard.intreface';
import * as DashboardActions from '../../../dashboard/store/dashboard.actions';
import { CommonModule, DatePipe } from '@angular/common';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import {
  selectMPGSTransactionDetails,
  selectMPGSTransactionDetailsLoading,
  selectMPGSTransactionDetailsError,
} from '../../../dashboard/store/dashboard.selectors';

@Component({
  selector: 'app-transaction-details-modal',
  standalone: true,
  imports: [CommonModule, MatDialogModule, MatProgressBarModule],
  providers: [DatePipe],
  template: `
    <div class="bg-white rounded-lg shadow-xl max-h-[90vh] flex flex-col">
      <!-- Header -->
      <div
        class="px-4 py-3 border-b border-gray-200 flex justify-between items-center bg-gray-50"
        >
        <div>
          <h2 class="text-lg font-semibold text-gray-800">
            Transaction Details
          </h2>
          <p class="text-xs text-gray-500 mt-0.5 truncate max-w-[300px]">
            Order ID: {{ data.orderId }}
          </p>
        </div>
        <button
          (click)="close()"
          class="px-3 py-1.5 bg-gray-100 hover:bg-gray-200 text-gray-600 rounded-md transition-colors flex items-center space-x-1"
          >
          <i class="fas fa-times text-sm"></i>
          <span class="text-sm">Close</span>
        </button>
      </div>
    
      <!-- Content -->
      <div class="p-4 overflow-y-auto">
        <!-- Loading State -->
        @if (loading$ | async) {
          <div class="py-4">
            <mat-progress-bar mode="indeterminate"></mat-progress-bar>
            <p class="text-center text-gray-500 mt-2 text-sm">
              Loading transaction details...
            </p>
          </div>
        }
    
        <!-- Error State -->
        @if (error$ | async; as error) {
          <div class="text-center py-4">
            <div class="text-red-500 text-4xl mb-2">
              <i class="fas fa-exclamation-circle"></i>
            </div>
            <h3 class="text-lg font-medium text-gray-700">
              Error Loading Details
            </h3>
            <p class="text-gray-500 mt-1 text-sm">{{ error }}</p>
          </div>
        }
    
        <!-- Transactions List -->
        @if (transactions$ | async; as transactions) {
          @if (!transactions || transactions.length === 0) {
            <div
              class="text-center py-6"
              >
              <div class="text-gray-400 text-4xl mb-2">
                <i class="far fa-file-alt"></i>
              </div>
              <h3 class="text-lg font-medium text-gray-700">
                No transactions found
              </h3>
              <p class="text-gray-500 mt-1 text-sm">
                No transaction details available for this order.
              </p>
            </div>
          }
          @if (transactions && transactions.length > 0) {
            <div
              class="space-y-3"
              >
              @for (transaction of transactions; track transaction) {
                <div
                  class="bg-white border border-gray-200 rounded-lg overflow-hidden hover:shadow-md transition-shadow"
                  >
                  <!-- Transaction Header -->
                  <div
                    class="px-4 py-3 bg-gray-50 border-b border-gray-200 flex items-center justify-between"
                    >
                    <div class="flex items-center space-x-3 min-w-0">
                      <div
                    [ngClass]="
                      getTransactionTypeClass(transaction.transactionType)
                    "
                        class="w-8 h-8 rounded-full flex items-center justify-center flex-shrink-0"
                        >
                        <i
                      [class]="
                        getTransactionTypeIcon(transaction.transactionType)
                      "
                          class="text-sm"
                        ></i>
                      </div>
                      <div class="min-w-0">
                        <h3 class="text-sm font-semibold text-gray-800 truncate">
                          {{ transaction.transactionType }}
                        </h3>
                        <p class="text-xs text-gray-500 truncate">
                          {{ transaction.orderDescription || 'No description' }}
                        </p>
                      </div>
                    </div>
                    <div class="text-right ml-4 flex-shrink-0">
                      <span
                        [ngClass]="getStatusClass(transaction.orderStatus)"
                        class="px-2 py-0.5 rounded-full text-xs font-medium whitespace-nowrap"
                        >
                        {{ transaction.orderStatus }}
                      </span>
                      <div
                        class="mt-1 text-base font-semibold"
                        [ngClass]="getAmountClass(transaction)"
                        >
                        {{ transaction.amount }} {{ transaction.currency }}
                      </div>
                    </div>
                  </div>
                  <!-- Transaction Details -->
                  <div class="p-4">
                    <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
                      <!-- Left Column -->
                      <div class="space-y-2">
                        <div class="flex items-center text-sm">
                          <span class="text-gray-500 w-28 flex-shrink-0"
                            >Transaction ID:</span
                            >
                            <span class="font-mono text-gray-800 truncate">{{
                              transaction.transactionId
                            }}</span>
                          </div>
                          <div class="flex items-center text-sm">
                            <span class="text-gray-500 w-28 flex-shrink-0"
                              >Time:</span
                              >
                              <span class="text-gray-800 truncate">
                                <i class="far fa-clock mr-1"></i>
                                {{ formatDate(transaction.timeOfRecord) }}
                              </span>
                            </div>
                            <div class="flex items-center text-sm">
                              <span class="text-gray-500 w-28 flex-shrink-0"
                                >Card Number:</span
                                >
                                <span class="font-mono text-gray-800 truncate">{{
                                  transaction.cardNumber
                                }}</span>
                              </div>
                              <div class="flex items-center text-sm">
                                <span class="text-gray-500 w-28 flex-shrink-0"
                                  >Cardholder:</span
                                  >
                                  <span class="text-gray-800 truncate">{{
                                    transaction.cardholderName
                                  }}</span>
                                </div>
                              </div>
                              <!-- Right Column -->
                              <div class="space-y-2">
                                <div class="flex items-center text-sm">
                                  <span class="text-gray-500 w-28 flex-shrink-0"
                                    >Gateway Code:</span
                                    >
                                    <span class="text-gray-800 truncate">{{
                                      transaction.gatewayCode
                                    }}</span>
                                  </div>
                                  <div class="flex items-center text-sm">
                                    <span class="text-gray-500 w-28 flex-shrink-0"
                                      >Recommendation:</span
                                      >
                                      <span class="text-gray-800 truncate">{{
                                        transaction.gatewayRecommendation
                                      }}</span>
                                    </div>
                                    <div class="flex items-center text-sm">
                                      <span class="text-gray-500 w-28 flex-shrink-0"
                                        >Merchant:</span
                                        >
                                        <span class="text-gray-800 truncate">{{
                                          transaction.merchantName
                                        }}</span>
                                      </div>
                                      <div class="flex items-center text-sm">
                                        <span class="text-gray-500 w-28 flex-shrink-0"
                                          >Brand:</span
                                          >
                                          <span class="text-gray-800 truncate">{{
                                            transaction.brand
                                          }}</span>
                                        </div>
                                      </div>
                                    </div>
                                    <!-- Additional Details -->
                                    <div class="mt-3 pt-3 border-t border-gray-100">
                                      <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
                                        <div class="space-y-2">
                                          <div class="flex items-center text-sm">
                                            <span class="text-gray-500 w-28 flex-shrink-0"
                                              >Auth Status:</span
                                              >
                                              <span class="text-gray-800 truncate">{{
                                                transaction.authenticationStatus
                                              }}</span>
                                            </div>
                                            <div class="flex items-center text-sm">
                                              <span class="text-gray-500 w-28 flex-shrink-0"
                                                >3DS Version:</span
                                                >
                                                <span class="text-gray-800 truncate">{{
                                                  transaction.threeDsVersion
                                                }}</span>
                                              </div>
                                            </div>
                                            <div class="space-y-2">
                                              <div class="flex items-center text-sm">
                                                <span class="text-gray-500 w-28 flex-shrink-0"
                                                  >IP Address:</span
                                                  >
                                                  <span class="text-gray-800 truncate">{{
                                                    transaction.ipAddress || 'N/A'
                                                  }}</span>
                                                </div>
                                                <div class="flex items-center text-sm">
                                                  <span class="text-gray-500 w-28 flex-shrink-0"
                                                    >Order Description:</span
                                                    >
                                                    <span class="text-gray-800 truncate">{{
                                                      transaction.orderDescription || 'N/A'
                                                    }}</span>
                                                  </div>
                                                </div>
                                              </div>
                                            </div>
                                          </div>
                                        </div>
                                      }
                                    </div>
                                  }
                                }
                              </div>
                            </div>
    `,
  styles: [
    `
      :host {
        display: block;
      }
    `,
  ],
})
export class TransactionDetailsModalComponent implements OnInit, OnDestroy {
  transactions$: Observable<MPGSTransaction[] | null>;
  loading$: Observable<boolean>;
  error$: Observable<string | null>;
  private destroy$ = new Subject<void>();

  constructor(
    public dialogRef: MatDialogRef<TransactionDetailsModalComponent>,
    @Inject(MAT_DIALOG_DATA) public data: { orderId: string },
    private store: Store,
    private datePipe: DatePipe
  ) {
    this.transactions$ = this.store.select(selectMPGSTransactionDetails);
    this.loading$ = this.store.select(selectMPGSTransactionDetailsLoading);
    this.error$ = this.store.select(selectMPGSTransactionDetailsError);
  }

  ngOnInit() {
    this.store.dispatch(
      DashboardActions.fetchMPGSTransactionDetails({
        orderId: this.data.orderId,
      })
    );
  }

  ngOnDestroy() {
    this.destroy$.next();
    this.destroy$.complete();
  }

  close() {
    this.dialogRef.close();
  }

  formatDate(dateString: string): string {
    return this.datePipe.transform(dateString, 'MMM dd, yyyy hh:mm:ss a') || '';
  }

  getStatusClass(status: string): string {
    switch (status?.toUpperCase()) {
      case 'CAPTURED':
        return 'bg-green-100 text-green-800';
      case 'AUTHENTICATION_UNSUCCESSFUL':
        return 'bg-red-100 text-red-800';
      case 'AUTHENTICATED':
        return 'bg-blue-100 text-blue-800';
      default:
        return 'bg-gray-100 text-gray-800';
    }
  }

  getStatusDotClass(status: string): string {
    switch (status?.toUpperCase()) {
      case 'CAPTURED':
        return 'text-green-500';
      case 'AUTHENTICATION_UNSUCCESSFUL':
        return 'text-red-500';
      case 'AUTHENTICATED':
        return 'text-blue-500';
      default:
        return 'text-gray-500';
    }
  }

  getTransactionTypeClass(type: string): string {
    switch (type?.toUpperCase()) {
      case 'PAYMENT':
        return 'bg-green-100 text-green-600';
      case 'REFUND':
        return 'bg-red-100 text-red-600';
      case 'AUTHENTICATION':
        return 'bg-blue-100 text-blue-600';
      default:
        return 'bg-gray-100 text-gray-600';
    }
  }

  getTransactionTypeIcon(type: string): string {
    switch (type?.toUpperCase()) {
      case 'PAYMENT':
        return 'fas fa-credit-card';
      case 'REFUND':
        return 'fas fa-undo';
      case 'AUTHENTICATION':
        return 'fas fa-shield-alt';
      default:
        return 'fas fa-exchange-alt';
    }
  }

  getAmountClass(transaction: MPGSTransaction): string {
    if (
      transaction.transactionType === 'PAYMENT' &&
      transaction.orderStatus === 'CAPTURED'
    ) {
      return 'text-green-600';
    } else if (
      transaction.transactionType === 'REFUND' &&
      transaction.orderStatus === 'CAPTURED'
    ) {
      return 'text-red-600';
    }
    return 'text-gray-700';
  }
}
