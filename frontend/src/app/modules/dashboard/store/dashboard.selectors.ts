import { createFeatureSelector, createSelector } from '@ngrx/store';
import {
  DashboardDataState,
  DashboardProfileState,
  MPGSTransactionsState,
  MPGSTransactionDetailsState,
} from './dashboard.reducer';
import { UserProfile } from '../interfaces/Dashboard.intreface';

// Dashboard Data Selectors
export const selectDashboardDataState =
  createFeatureSelector<DashboardDataState>('dashboardData');

export const selectDashboardData = createSelector(
  selectDashboardDataState,
  (state) => state.dashboardData
);

export const selectDashboardDataLoading = createSelector(
  selectDashboardDataState,
  (state) => state.loading
);

export const selectDashboardDataError = createSelector(
  selectDashboardDataState,
  (state) => state.error
);

// Profile Selectors
export const selectDashboardProfileState =
  createFeatureSelector<DashboardProfileState>('dashboardProfile');

export const selectDashboardProfile = createSelector(
  selectDashboardProfileState,
  (state) => state.profile
);

export const selectDashboardProfileLoading = createSelector(
  selectDashboardProfileState,
  (state) => state.loading
);

export const selectDashboardProfileError = createSelector(
  selectDashboardProfileState,
  (state) => state.error
);

// User Profile Selectors
export const selectUserProfile = createFeatureSelector<{
  data: UserProfile | null;
}>('userProfile');

export const selectUserProfileData = createSelector(
  selectUserProfile,
  (state) => state.data
);

export const selectUserProfileLoading = createSelector(
  selectUserProfile,
  () => false
);

export const selectUserProfileError = createSelector(
  selectUserProfile,
  () => null
);

// MPGS Transactions Selectors
export const selectMPGSTransactionsState =
  createFeatureSelector<MPGSTransactionsState>('mpgsTransactions');

export const selectMPGSTransactions = createSelector(
  selectMPGSTransactionsState,
  (state) => state.transactions
);

export const selectMPGSTransactionsLoading = createSelector(
  selectMPGSTransactionsState,
  (state) => state.loading
);

export const selectMPGSTransactionsError = createSelector(
  selectMPGSTransactionsState,
  (state) => state.error
);

// MPGS Transaction Details Selectors
export const selectMPGSTransactionDetailsState =
  createFeatureSelector<MPGSTransactionDetailsState>('mpgsTransactionDetails');

export const selectMPGSTransactionDetails = createSelector(
  selectMPGSTransactionDetailsState,
  (state) => state.details
);

export const selectMPGSTransactionDetailsLoading = createSelector(
  selectMPGSTransactionDetailsState,
  (state) => state.loading
);

export const selectMPGSTransactionDetailsError = createSelector(
  selectMPGSTransactionDetailsState,
  (state) => state.error
);
