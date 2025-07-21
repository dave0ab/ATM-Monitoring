import { Injectable } from '@angular/core';
import { Actions, createEffect, ofType } from '@ngrx/effects';
import { catchError, map, mergeMap, tap, finalize } from 'rxjs/operators';
import { of } from 'rxjs';
import * as AtmAccessActions from './access.actions';
import { AtmService } from '../../services/atm.service';

@Injectable()
export class AtmAccessEffects {
  constructor(private actions$: Actions, private atmService: AtmService) {
    // Debug subscription to all actions
    this.actions$.subscribe((action) => {});
  }

  saveAtmAccess$ = createEffect(() =>
    this.actions$.pipe(
      ofType(AtmAccessActions.saveAtmAccess),
      mergeMap(({ atmAccesses }) => {
        return this.atmService.saveAtmAccess(atmAccesses).pipe(
          map((response) =>
            AtmAccessActions.saveAtmAccessSuccess({ atmAccesses: response })
          ),
          catchError((error) => {
            return of(
              AtmAccessActions.saveAtmAccessFailure({
                error: error.message || 'Failed to save ATM access',
              })
            );
          })
        );
      })
    )
  );

  fetchAtmAccess$ = createEffect(() =>
    this.actions$.pipe(
      ofType(AtmAccessActions.fetchAtmAccess),
      mergeMap(({ userId }) =>
        this.atmService.fetchAtmAccess(userId).pipe(
          map((atmAccesses) =>
            AtmAccessActions.fetchAtmAccessSuccess({ atmAccesses })
          ),
          catchError((error) =>
            of(
              AtmAccessActions.fetchAtmAccessFailure({
                error: error.message || 'Failed to fetch ATM access',
              })
            )
          )
        )
      )
    )
  );
}
