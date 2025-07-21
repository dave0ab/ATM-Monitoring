import { createAction, createFeatureSelector, props } from '@ngrx/store';
import {
  DashboardData,
  DashboardProfile,
  UserProfile,
  DashboardSummary,
  MPGSTransactionResponse,
  MPGSTransaction,
} from '../interfaces/Dashboard.intreface';
import { createSelector } from '@ngrx/store';
import { selectMPGSTransactionsState } from '../../atmMonitor/store/selectors/mpgs-transactions.selectors';

export const fetchDashboardData = createAction(
  '[Dashboard] Fetch Dashboard Data'
);
export const fetchDashboardDataSuccess = createAction(
  '[Dashboard] Fetch Data Success',
  props<{ dashboardData: DashboardSummary }>()
);
export const fetchDashboardDataFailure = createAction(
  '[Dashboard] Fetch Data Failure',
  props<{ error: string }>()
);
export const fetchDashboardDataClear = createAction('[Dashboard] Clear Data');

export const fetchUserProfile = createAction('[Dashboard] Fetch User Profile');

export const fetchUserProfileSuccess = createAction(
  '[Dashboard] Fetch User Profile Success',
  props<{ userProfile: UserProfile }>()
);

export const fetchUserProfileFailure = createAction(
  '[Dashboard] Fetch User Profile Failure',
  props<{ error: string }>()
);

export const fetchDashboardProfile = createAction('[Dashboard] Fetch Profile');
export const fetchDashboardProfileSuccess = createAction(
  '[Dashboard] Fetch Profile Success',
  props<{ profile: DashboardProfile }>()
);
export const fetchDashboardProfileFailure = createAction(
  '[Dashboard] Fetch Profile Failure',
  props<{ error: string }>()
);

// MPGS Transactions Actions
export const fetchMPGSTransactions = createAction(
  '[Dashboard] Fetch MPGS Transactions',
  props<{
    page: number;
    size: number;
    orderStatus?: string;
    search?: string;
    brand?: string;
    startDate: string;
    endDate: string;
  }>()
);

export const fetchMPGSTransactionsSuccess = createAction(
  '[Dashboard] Fetch MPGS Transactions Success',
  props<{ transactions: MPGSTransactionResponse }>()
);

export const fetchMPGSTransactionsFailure = createAction(
  '[Dashboard] Fetch MPGS Transactions Failure',
  props<{ error: string }>()
);

// MPGS Transaction Details Actions
export const fetchMPGSTransactionDetails = createAction(
  '[Dashboard] Fetch MPGS Transaction Details',
  props<{ orderId: string }>()
);

export const fetchMPGSTransactionDetailsSuccess = createAction(
  '[Dashboard] Fetch MPGS Transaction Details Success',
  props<{ details: MPGSTransaction[] }>()
);

export const fetchMPGSTransactionDetailsFailure = createAction(
  '[Dashboard] Fetch MPGS Transaction Details Failure',
  props<{ error: string }>()
);

export interface MPGSTransactionsState {
  transactions: MPGSTransactionResponse | null;
  loading: boolean;
  error: string | null;
  transactionDetails: MPGSTransaction[] | null;
}

export const initialMPGSTransactionsState: MPGSTransactionsState = {
  transactions: null,
  loading: false,
  error: null,
  transactionDetails: null,
};
