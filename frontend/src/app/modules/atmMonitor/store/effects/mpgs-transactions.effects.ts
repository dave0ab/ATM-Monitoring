import { Injectable } from '@angular/core';
import { Actions, createEffect, ofType } from '@ngrx/effects';
import { of } from 'rxjs';
import { map, mergeMap, catchError } from 'rxjs/operators';
import * as MPGSTransactionsActions from '../actions/mpgs-transactions.actions';
import { MPGSTransactionsService } from '../../services/mpgs-transactions.service';

@Injectable()
export class MPGSTransactionsEffects {
  loadMPGSTransactions$ = createEffect(() =>
    this.actions$.pipe(
      ofType(MPGSTransactionsActions.loadMPGSTransactions),
      mergeMap(({ page, pageSize, status, brand, search }) =>
        this.mpgsTransactionsService
          .getTransactions(page, pageSize, status, brand, search)
          .pipe(
            map((response) =>
              MPGSTransactionsActions.loadMPGSTransactionsSuccess({
                transactions: response.transactions,
                totalItems: response.totalItems,
              })
            ),
            catchError((error) =>
              of(
                MPGSTransactionsActions.loadMPGSTransactionsFailure({
                  error: error.message,
                })
              )
            )
          )
      )
    )
  );

  constructor(
    private actions$: Actions,
    private mpgsTransactionsService: MPGSTransactionsService
  ) {}
}
