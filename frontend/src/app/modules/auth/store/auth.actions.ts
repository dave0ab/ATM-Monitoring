import { createAction, props } from '@ngrx/store';
import {
  Users,
  FetchUsersState,
  RegisterUserRequest,
  RegisterUserResponse,
  UpdateUserRequest,
  UpdateUserResponse,
} from '../interfaces/user.interface';

export const sessionExpiredAction = createAction(
  '[Auth] Session Expired',
  props<{ error: string }>()
);
export const clearSessionExpiredAction = createAction(
  '[Auth] Clear Session Expired'
);

export const loadUsers = createAction(
  '[Users] Load Users',
  props<{ pageNumber: number; pageSize: number; search?: string }>()
);

export const loadUsersSuccess = createAction(
  '[Users] Load Users Success',
  props<{
    response: {
      content: Users[];
      totalElements: number;
      totalPages: number;
      pageable: any;
    };
  }>()
);

export const loadUsersFailure = createAction(
  '[Users] Load Users Failure',
  props<{ error: any }>()
);

export const registerUser = createAction(
  '[Auth] Register User',
  props<{ request: RegisterUserRequest }>()
);

export const registerUserSuccess = createAction(
  '[Auth] Register User Success',
  props<{ response: RegisterUserResponse }>()
);

export const registerUserFailure = createAction(
  '[Auth] Register User Failure',
  props<{
    error: string;
    validationErrors?: { [key: string]: string } | null;
  }>()
);

export const updateUser = createAction(
  '[Auth] Update User',
  props<{ userId: number; request: UpdateUserRequest }>()
);

export const updateUserSuccess = createAction(
  '[Auth] Update User Success',
  props<{ response: UpdateUserResponse }>()
);

export const updateUserFailure = createAction(
  '[Auth] Update User Failure',
  props<{
    error: string;
    validationErrors?: { [key: string]: string } | null;
  }>()
);

export const clearUpdateUserState = createAction(
  '[Auth] Clear Update User State'
);

export const clearUpdateState = createAction('[Auth] Clear Update State');
export const clearRegistrationState = createAction(
  '[Auth] Clear Registration State'
);
