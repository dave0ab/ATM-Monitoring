import { createFeatureSelector, createSelector } from '@ngrx/store';
import { MPGSTransaction } from '../../models/mpgs-transaction.model';

export interface MPGSTransactionsState {
  transactions: MPGSTransaction[];
  loading: boolean;
  error: string | null;
  totalItems: number;
  transactionDetails: MPGSTransaction[] | null; // Add this line
}

export const selectMPGSTransactionsState =
  createFeatureSelector<MPGSTransactionsState>('mpgsTransactions');

export const selectMPGSTransactionDetails = createSelector(
  selectMPGSTransactionsState,
  (state) => state.transactionDetails
);

export const selectAllMPGSTransactions = createSelector(
  selectMPGSTransactionsState,
  (state: MPGSTransactionsState) => state.transactions
);

export const selectMPGSTransactionsLoading = createSelector(
  selectMPGSTransactionsState,
  (state: MPGSTransactionsState) => state.loading
);

export const selectMPGSTransactionsError = createSelector(
  selectMPGSTransactionsState,
  (state: MPGSTransactionsState) => state.error
);

export const selectMPGSTransactionsTotalItems = createSelector(
  selectMPGSTransactionsState,
  (state: MPGSTransactionsState) => state.totalItems
);
