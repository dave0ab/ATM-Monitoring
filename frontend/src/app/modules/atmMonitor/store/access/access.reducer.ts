import { createReducer, on } from '@ngrx/store';

import * as AtmAccessActions from './access.actions';
import { AtmAccessState } from '../../interfaces/atm-access.interface';

export const initialState: AtmAccessState = {
  atmAccesses: [],
  loading: false,
  error: null,
  success: null,
};

export const atmAccessFeatureKey = 'atmAccess';

export const atmAccessReducer = createReducer(
  initialState,
  on(AtmAccessActions.saveAtmAccess, (state) => ({
    ...state,
    loading: true,
    error: null,
    success: null,
  })),
  on(AtmAccessActions.saveAtmAccessSuccess, (state, { atmAccesses }) => ({
    ...state,
    atmAccesses,
    loading: false,
    error: null,
    success: 'ATM access saved successfully',
  })),
  on(AtmAccessActions.saveAtmAccessFailure, (state, { error }) => ({
    ...state,
    loading: false,
    error,
    success: null,
  })),
  on(AtmAccessActions.fetchAtmAccess, (state) => ({
    ...state,
    loading: true,
    error: null,
    success: null,
  })),
  on(AtmAccessActions.fetchAtmAccessSuccess, (state, { atmAccesses }) => ({
    ...state,
    atmAccesses,
    loading: false,
    error: null,
  })),
  on(AtmAccessActions.fetchAtmAccessFailure, (state, { error }) => ({
    ...state,
    loading: false,
    error,
    success: null,
  }))
);
