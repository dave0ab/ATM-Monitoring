import { createFeatureSelector, createSelector } from '@ngrx/store';
import { LocationState, locationFeatureKey } from './location.reducer';

export const selectLocationState =
  createFeatureSelector<LocationState>(locationFeatureKey);

export const selectLocation = createSelector(
  selectLocationState,
  (state) => state.location
);

export const selectLocationLoading = createSelector(
  selectLocationState,
  (state) => state.loading
);

export const selectLocationError = createSelector(
  selectLocationState,
  (state) => state.error
);
