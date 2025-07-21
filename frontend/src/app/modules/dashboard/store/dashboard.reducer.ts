import { createReducer, on } from '@ngrx/store';

import * as DashboardActions from './dashboard.actions';
import {
  DashboardData,
  DashboardProfile,
  UserProfile,
  DashboardSummary,
  MPGSTransactionResponse,
  MPGSTransaction,
} from '../interfaces/Dashboard.intreface';

// Dashboard Data State
export interface DashboardDataState {
  dashboardData: DashboardSummary | null;
  loading: boolean;
  error: string | null;
}

const initialDashboardDataState: DashboardDataState = {
  dashboardData: null,
  loading: false,
  error: null,
};

export const dashboardDataFeatureKey = 'dashboardData';

// Profile State
export interface DashboardProfileState {
  profile: DashboardProfile | null;
  loading: boolean;
  error: string | null;
}

const initialProfileState: DashboardProfileState = {
  profile: null,
  loading: false,
  error: null,
};

export const dashboardProfileFeatureKey = 'dashboardProfile';

// Dashboard Data Reducer
export const dashboardDataReducer = createReducer(
  initialDashboardDataState,
  on(DashboardActions.fetchDashboardData, (state) => ({
    ...state,
    loading: true,
    error: null,
  })),
  on(
    DashboardActions.fetchDashboardDataSuccess,
    (state, { dashboardData }) => ({
      ...state,
      dashboardData,
      loading: false,
    })
  ),
  on(DashboardActions.fetchDashboardDataFailure, (state, { error }) => ({
    ...state,
    loading: false,
    error,
  })),
  on(DashboardActions.fetchDashboardDataClear, (state) => ({
    ...state,
    dashboardData: null,
    loading: false,
    error: null,
  }))
);

// User Profile State
export interface UserProfileState {
  data: UserProfile | null;
}

const initialUserProfileState: UserProfileState = {
  data: null,
};

export const userProfileFeatureKey = 'userProfile';

export const userProfileReducer = createReducer(
  initialUserProfileState,
  on(DashboardActions.fetchUserProfile, (state) => ({
    ...state,
  })),
  on(DashboardActions.fetchUserProfileSuccess, (state, { userProfile }) => ({
    ...state,
    data: userProfile,
  })),
  on(DashboardActions.fetchUserProfileFailure, (state, { error }) => ({
    ...state,
    data: null,
  }))
);

// MPGS Transactions State
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

export const mpgsTransactionsFeatureKey = 'mpgsTransactions';

export const mpgsTransactionsReducer = createReducer(
  initialMPGSTransactionsState,
  on(DashboardActions.fetchMPGSTransactions, (state) => ({
    ...state,
    loading: true,
    error: null,
  })),
  on(
    DashboardActions.fetchMPGSTransactionsSuccess,
    (state, { transactions }) => ({
      ...state,
      transactions,
      loading: false,
    })
  ),
  on(DashboardActions.fetchMPGSTransactionsFailure, (state, { error }) => ({
    ...state,
    loading: false,
    error,
  }))
);

// MPGS Transaction Details State
export interface MPGSTransactionDetailsState {
  details: MPGSTransaction[] | null;
  loading: boolean;
  error: string | null;
}

export const initialMPGSTransactionDetailsState: MPGSTransactionDetailsState = {
  details: null,
  loading: false,
  error: null,
};

export const mpgsTransactionDetailsFeatureKey = 'mpgsTransactionDetails';

export const mpgsTransactionDetailsReducer = createReducer(
  initialMPGSTransactionDetailsState,
  on(DashboardActions.fetchMPGSTransactionDetails, (state) => ({
    ...state,
    loading: true,
    error: null,
  })),
  on(
    DashboardActions.fetchMPGSTransactionDetailsSuccess,
    (state, { details }) => ({
      ...state,
      details,
      loading: false,
    })
  ),
  on(
    DashboardActions.fetchMPGSTransactionDetailsFailure,
    (state, { error }) => ({
      ...state,
      loading: false,
      error,
    })
  )
);
