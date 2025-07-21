import { createFeatureSelector, createSelector } from '@ngrx/store';
import {
  AuthState,
  UpdateUserState,
  authFeatureKey,
  updateUserFeatureKey,
} from './auth.reducer';
import { userFeatureKey } from './auth.reducer';
import { FetchUsersState } from '../interfaces/user.interface';
import { RegistrationState, registrationFeatureKey } from './auth.reducer';

const authSelectorFeature = createFeatureSelector<AuthState>(authFeatureKey);

export const selectSessionExpired = createSelector(
  authSelectorFeature,
  (state: AuthState) => state.sessionExpired
);

export const selectFetchUsersState =
  createFeatureSelector<FetchUsersState>(userFeatureKey);

export const selectUsers = createSelector(
  selectFetchUsersState,
  (state: FetchUsersState) => state?.content || []
);

export const selectUsersLoading = createSelector(
  selectFetchUsersState,
  (state: FetchUsersState) => state.loading
);

export const selectUsersError = createSelector(
  selectFetchUsersState,
  (state: FetchUsersState) => state.error
);

export const selectTotalPages = createSelector(
  selectFetchUsersState,
  (state: FetchUsersState) => state.totalPages
);

export const selectTotalElements = createSelector(
  selectFetchUsersState,
  (state: FetchUsersState) => state?.totalElements || 0
);

export const selectSuccess = createSelector(
  selectFetchUsersState,
  (state: FetchUsersState) => state.success
);

const selectRegistrationFeature = createFeatureSelector<RegistrationState>(
  registrationFeatureKey
);

export const selectRegistrationLoading = createSelector(
  selectRegistrationFeature,
  (state) => state.loading
);

export const selectRegistrationError = createSelector(
  selectRegistrationFeature,
  (state) => state.error
);

export const selectRegistrationValidationErrors = createSelector(
  selectRegistrationFeature,
  (state) => state.validationErrors
);

export const selectRegistrationSuccess = createSelector(
  selectRegistrationFeature,
  (state) => state.success
);

const selectUpdateUserState =
  createFeatureSelector<UpdateUserState>(updateUserFeatureKey);

// Replace the existing update selectors
export const selectUpdateUserLoading = createSelector(
  selectUpdateUserState,
  (state: UpdateUserState) => state.loading
);

export const selectUpdateUserError = createSelector(
  selectUpdateUserState,
  (state: UpdateUserState) => state.error
);

export const selectUpdateUserValidationErrors = createSelector(
  selectUpdateUserState,
  (state: UpdateUserState) => state.validationErrors
);

export const selectUpdateUserSuccess = createSelector(
  selectUpdateUserState,
  (state: UpdateUserState) => state.success
);
