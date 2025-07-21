import { createFeatureSelector, createSelector } from '@ngrx/store';
import { AtmAccessState } from '../../interfaces/atm-access.interface';

export const selectAtmAccessState =
  createFeatureSelector<AtmAccessState>('atmAccess');

export const selectAtmAccesses = createSelector(
  selectAtmAccessState,
  (state) => state.atmAccesses
);

export const selectAtmAccessLoading = createSelector(
  selectAtmAccessState,
  (state) => state.loading
);

export const selectAtmAccessError = createSelector(
  selectAtmAccessState,
  (state) => state.error
);

export const selectAtmAccessSuccess = createSelector(
  selectAtmAccessState,
  (state) => state.success
);
