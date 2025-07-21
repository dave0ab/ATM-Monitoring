import { Actions, createEffect, ofType } from '@ngrx/effects';
import { catchError, map, mergeMap, of, tap } from 'rxjs';
import * as AuthActions from './auth.actions';
import { Injectable } from '@angular/core';
import { Router } from '@angular/router';
import { Store } from '@ngrx/store';
import { LocalStorageService } from 'src/app/core/services/localstorage.service';
import { AuthService } from '../services/auth.service';

@Injectable()
export class AuthEffects {
  constructor(
    private actions$: Actions,
    private store: Store,
    private authService: AuthService,
    private localStorageService: LocalStorageService,
    private router: Router
  ) {}

  sessionExpired$ = createEffect(
    () =>
      this.actions$.pipe(
        ofType(AuthActions.sessionExpiredAction),
        tap((error) => {
          this.authService.logOut();
        })
      ),
    { dispatch: false } // Set dispatch to false to prevent re-dispatching the action
  );

  loadUsers$ = createEffect(() =>
    this.actions$.pipe(
      ofType(AuthActions.loadUsers),
      mergeMap(({ pageNumber, pageSize, search }) =>
        this.authService.getUsers(pageNumber, pageSize, search).pipe(
          map((response) =>
            AuthActions.loadUsersSuccess({
              response: {
                content: response.content,
                totalElements: response.totalElements ?? 0,
                totalPages: response.totalPages ?? 0,
                pageable: {},
              },
            })
          ),
          catchError((error) => of(AuthActions.loadUsersFailure({ error })))
        )
      )
    )
  );

  updateUser$ = createEffect(() =>
    this.actions$.pipe(
      ofType(AuthActions.updateUser),
      tap((action) => console.log('')),
      mergeMap(({ userId, request }) =>
        this.authService.updateUser(userId, request).pipe(
          tap((response) => console.log('')),
          map((response) => {
            console.log('');
            return AuthActions.updateUserSuccess({ response });
          }),
          catchError((error) => {
            console.error('Update user error:', error);
            // Extract meaningful error message from backend response
            let errorMessage = 'Update failed';
            let validationErrors = null;

            if (error.error) {
              const errorBody = error.error;

              if (errorBody.validationErrors) {
                // Backend validation errors with field-specific messages
                validationErrors = errorBody.validationErrors;
                errorMessage =
                  errorBody.message || 'Please fix the validation errors';
              } else if (errorBody.message) {
                // Single error message from backend (for any status)
                errorMessage = errorBody.message;
              } else if (
                errorBody.error &&
                typeof errorBody.error === 'string'
              ) {
                // Spring Boot error format: {statusCode, timestamp, error, description}
                if (errorBody.error.includes('Validation failed')) {
                  errorMessage =
                    'Please check your input and fix the validation errors';
                } else {
                  errorMessage = errorBody.error;
                }
              } else if (typeof errorBody === 'string') {
                // Error body is a string
                errorMessage = errorBody;
              }
            } else if (error.status === 500) {
              errorMessage = 'Server error occurred. Please try again later.';
            } else if (error.status === 404) {
              errorMessage = 'User not found.';
            } else {
              errorMessage =
                error.message || `Request failed with status ${error.status}`;
            }

            return of(
              AuthActions.updateUserFailure({
                error: errorMessage,
                validationErrors: validationErrors,
              })
            );
          })
        )
      )
    )
  );

  registerUser$ = createEffect(() =>
    this.actions$.pipe(
      ofType(AuthActions.registerUser),
      mergeMap(({ request }) =>
        this.authService.registerUser(request).pipe(
          map((response) => AuthActions.registerUserSuccess({ response })),
          catchError((error) => {
            // Extract meaningful error message from backend response
            let errorMessage = 'Registration failed';
            let validationErrors = null;

            if (error.error) {
              const errorBody = error.error;

              if (errorBody.validationErrors) {
                // Backend validation errors with field-specific messages
                validationErrors = errorBody.validationErrors;
                errorMessage =
                  errorBody.message || 'Please fix the validation errors';
              } else if (errorBody.message) {
                // Single error message from backend (for any status)
                errorMessage = errorBody.message;
              } else if (typeof errorBody === 'string') {
                // Error body is a string
                errorMessage = errorBody;
              }
            } else if (error.status === 500) {
              errorMessage = 'Server error occurred. Please try again later.';
            } else if (error.status === 404) {
              errorMessage = 'Service not found. Please contact support.';
            } else {
              errorMessage =
                error.message || `Request failed with status ${error.status}`;
            }

            return of(
              AuthActions.registerUserFailure({
                error: errorMessage,
                validationErrors: validationErrors,
              })
            );
          })
        )
      )
    )
  );
}
