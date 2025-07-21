import { createAction, props } from '@ngrx/store';
import { MPGSTransaction } from '../../models/mpgs-transaction.model';

export const loadMPGSTransactions = createAction(
  '[MPGS Transactions] Load Transactions',
  props<{
    page: number;
    pageSize: number;
    status?: string;
    brand?: string;
    search?: string;
  }>()
);

export const loadMPGSTransactionsSuccess = createAction(
  '[MPGS Transactions] Load Transactions Success',
  props<{
    transactions: MPGSTransaction[];
    totalItems: number;
  }>()
);

export const loadMPGSTransactionsFailure = createAction(
  '[MPGS Transactions] Load Transactions Failure',
  props<{ error: string }>()
);
