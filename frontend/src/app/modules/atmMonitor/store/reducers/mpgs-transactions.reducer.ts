import { createReducer, on } from '@ngrx/store';
import * as MPGSTransactionsActions from '../actions/mpgs-transactions.actions';
import { MPGSTransactionsState } from '../selectors/mpgs-transactions.selectors';

export const initialState: MPGSTransactionsState = {
  transactions: [],
  loading: false,
  error: null,
  totalItems: 0,
};

export const mpgsTransactionsReducer = createReducer(
  initialState,
  on(MPGSTransactionsActions.loadMPGSTransactions, (state) => ({
    ...state,
    loading: true,
    error: null,
  })),
  on(
    MPGSTransactionsActions.loadMPGSTransactionsSuccess,
    (state, { transactions, totalItems }) => ({
      ...state,
      transactions,
      totalItems,
      loading: false,
      error: null,
    })
  ),
  on(
    MPGSTransactionsActions.loadMPGSTransactionsFailure,
    (state, { error }) => ({
      ...state,
      loading: false,
      error,
    })
  )
);
