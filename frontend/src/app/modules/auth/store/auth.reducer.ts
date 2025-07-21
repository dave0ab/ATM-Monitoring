import { createReducer, on } from '@ngrx/store';
import * as AuthActions from './auth.actions';
import {
  FetchUsersState,
  RegisterUserResponse,
  Users,
  UpdateUserRequest,
  UpdateUserResponse,
} from '../interfaces/user.interface';
import * as UserActions from './auth.actions';

export interface AuthState {
  sessionExpired: string | null;
}

export const authState: AuthState = {
  sessionExpired: null,
};

export const authFeatureKey = 'auth';

export const authReducer = createReducer(
  authState,
  on(
    AuthActions.sessionExpiredAction,
    (state: AuthState, { error }: { error: string }): AuthState => {
      return {
        ...state,
        sessionExpired: error,
      };
    }
  ),
  on(AuthActions.clearSessionExpiredAction, (state) => ({
    ...state,
    sessionExpired: null,
  }))
);

export interface UpdateUserState {
  loading: boolean;
  error: string | null;
  success: UpdateUserResponse | null;
  validationErrors: { [key: string]: string } | null;
}

export const updateUserFeatureKey = 'updateUser';

export const initialUpdateUserState: UpdateUserState = {
  loading: false,
  error: null,
  success: null,
  validationErrors: null,
};

export const updateUserReducer = createReducer(
  initialUpdateUserState,
  on(AuthActions.updateUser, (state) => ({
    ...state,
    loading: true,
    error: null,
    success: null,
  })),
  on(AuthActions.updateUserSuccess, (state, { response }) => ({
    ...state,
    loading: false,
    success: response,
    error: null,
  })),
  on(AuthActions.updateUserFailure, (state, { error, validationErrors }) => ({
    ...state,
    loading: false,
    error,
    success: null,
    validationErrors: validationErrors || null,
  })),
  on(AuthActions.clearUpdateState, () => ({
    ...initialUpdateUserState,
  }))
);

export const initialUserState: FetchUsersState = {
  content: [],
  loading: false,
  success: null,
  error: null,
  totalPages: 0,
  totalElements: 0,
};

export const userFeatureKey = 'users';

export const userReducer = createReducer(
  initialUserState,
  on(UserActions.loadUsers, (state) => ({
    ...state,
    loading: true,
    error: null,
  })),
  on(UserActions.loadUsersSuccess, (state, { response }) => {
    return {
      ...state,
      content: response.content,
      totalElements: response.totalElements,
      totalPages: response.totalPages,
      loading: false,
      error: null,
    };
  }),
  on(UserActions.loadUsersFailure, (state, { error }) => ({
    ...state,
    error,
    loading: false,
  })),
  on(AuthActions.loadUsers, (state) => ({
    ...state,
    loading: true,
    error: null,
  })),
  on(AuthActions.updateUserSuccess, (state, { response }) => {
    const updatedContent = state.content.map((user) =>
      user.id === response.id
        ? {
            ...user,
            ...response,
            role: response.role as 'ADMIN' | 'IT' | 'BRANCH' | 'OMNI_CHANNEL',
            status: response.status as 'ACTIVE' | 'INACTIVE',
          }
        : user
    );
    return {
      ...state,
      content: updatedContent,
      loading: false,
      error: null,
    };
  })
);

// Separate feature key for registration
export const registrationFeatureKey = 'registration';

// Registration state interface
export interface RegistrationState {
  loading: boolean;
  error: string | null;
  success: RegisterUserResponse | null;
  submitted: boolean;
  validationErrors: { [key: string]: string } | null;
  lastAttemptTimestamp: number | null;
  retryCount: number;
}

// Initial state for registration
export const initialRegistrationState: RegistrationState = {
  loading: false,
  error: null,
  success: null,
  submitted: false,
  validationErrors: null,
  lastAttemptTimestamp: null,
  retryCount: 0,
};

// Registration reducer with correct action names
export const registrationReducer = createReducer(
  initialRegistrationState,
  on(AuthActions.registerUser, (state) => ({
    ...state,
    loading: true,
    error: null,
    success: null,
  })),
  on(AuthActions.registerUserSuccess, (state, { response }) => ({
    ...state,
    loading: false,
    success: response,
    error: null,
  })),
  on(AuthActions.registerUserFailure, (state, { error, validationErrors }) => ({
    ...state,
    loading: false,
    error,
    success: null,
    validationErrors: validationErrors || null,
  })),
  on(AuthActions.clearRegistrationState, () => ({
    ...initialRegistrationState,
  }))
);

// Helper function to parse validation errors
function parseValidationErrors(error: string): { [key: string]: string } {
  try {
    return JSON.parse(error);
  } catch {
    return { general: error };
  }
}

// Selectors
export const getRegistrationState = (state: RegistrationState) => state;
export const getRegistrationLoading = (state: RegistrationState) =>
  state.loading;
export const getRegistrationError = (state: RegistrationState) => state.error;
export const getRegistrationSuccess = (state: RegistrationState) =>
  state.success;
export const getRegistrationSubmitted = (state: RegistrationState) =>
  state.submitted;
export const getRegistrationValidationErrors = (state: RegistrationState) =>
  state.validationErrors;
export const getRegistrationRetryCount = (state: RegistrationState) =>
  state.retryCount;
export const getLastRegistrationAttempt = (state: RegistrationState) =>
  state.lastAttemptTimestamp;

// Helper function to check if user can retry registration
export const canRetryRegistration = (state: RegistrationState): boolean => {
  const MAX_RETRIES = 3;
  const RETRY_COOLDOWN = 5 * 60 * 1000; // 5 minutes in milliseconds

  if (state.retryCount >= MAX_RETRIES && state.lastAttemptTimestamp) {
    const timeSinceLastAttempt = Date.now() - state.lastAttemptTimestamp;
    return timeSinceLastAttempt >= RETRY_COOLDOWN;
  }

  return state.retryCount < MAX_RETRIES;
};
