import { createReducer, on } from '@ngrx/store';
import { AtmLocation } from '../../interfaces/atm-location.interface';
import * as LocationActions from './location.actions';

export const locationFeatureKey = 'atmLocation';

export interface LocationState {
  location: AtmLocation | null;
  loading: boolean;
  error: string | null;
}

export const initialState: LocationState = {
  location: null,
  loading: false,
  error: null,
};

export const locationReducer = createReducer(
  initialState,

  // Get Location
  on(LocationActions.getLocation, (state) => ({
    ...state,
    loading: true,
    error: null,
  })),

  on(LocationActions.getLocationSuccess, (state, { location }) => ({
    ...state,
    location,
    loading: false,
    error: null,
  })),

  on(LocationActions.getLocationFailure, (state, { error }) => ({
    ...state,
    loading: false,
    error,
  })),

  // Update Location
  on(LocationActions.updateLocation, (state) => ({
    ...state,
    loading: true,
    error: null,
  })),

  on(LocationActions.updateLocationSuccess, (state, { location }) => ({
    ...state,
    location,
    loading: false,
    error: null,
  })),

  on(LocationActions.updateLocationFailure, (state, { error }) => ({
    ...state,
    loading: false,
    error,
  }))
);
