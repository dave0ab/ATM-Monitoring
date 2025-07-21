import { Injectable } from '@angular/core';
import { Actions, createEffect, ofType } from '@ngrx/effects';
import { Store } from '@ngrx/store';
import { of } from 'rxjs';
import { catchError, map, mergeMap, withLatestFrom, tap } from 'rxjs/operators';
import { DashboardService } from '../services/dashboard.service';

import * as DashboardActions from './dashboard.actions';
import {
  DashboardData,
  UserProfile,
  DashboardSummary,
  MPGSTransactionResponse,
} from '../interfaces/Dashboard.intreface';

@Injectable()
export class DashboardEffects {
  constructor(
    private store: Store,
    private actions$: Actions,
    private dashboardService: DashboardService
  ) {}

  fetchDashboardData = createEffect(() =>
    this.actions$.pipe(
      ofType(DashboardActions.fetchDashboardData),
      mergeMap(() =>
        this.dashboardService.getDashboard().pipe(
          map((dashboardData: DashboardSummary) =>
            DashboardActions.fetchDashboardDataSuccess({ dashboardData })
          ),
          catchError((error) =>
            of(
              DashboardActions.fetchDashboardDataFailure({
                error: error.error?.description || 'An error occurred',
              })
            )
          )
        )
      )
    )
  );

  fetchUserProfile = createEffect(() =>
    this.actions$.pipe(
      ofType(DashboardActions.fetchUserProfile),
      mergeMap(() =>
        this.dashboardService.getUserProfile().pipe(
          map((userProfile: UserProfile) =>
            DashboardActions.fetchUserProfileSuccess({ userProfile })
          ),
          catchError((error) =>
            of(
              DashboardActions.fetchUserProfileFailure({
                error: error.error?.description || 'An error occurred',
              })
            )
          )
        )
      )
    )
  );

  fetchMPGSTransactions = createEffect(() =>
    this.actions$.pipe(
      ofType(DashboardActions.fetchMPGSTransactions),
      mergeMap(
        ({ page, size, orderStatus, search, brand, startDate, endDate }) => {
          return this.dashboardService
            .getMPGSTransactions(
              page,
              size,
              orderStatus,
              search,
              brand,
              startDate,
              endDate
            )
            .pipe(
              map((transactions: MPGSTransactionResponse) =>
                DashboardActions.fetchMPGSTransactionsSuccess({ transactions })
              ),
              catchError((error) => {
                return of(
                  DashboardActions.fetchMPGSTransactionsFailure({
                    error: error.error?.description || 'An error occurred',
                  })
                );
              })
            );
        }
      )
    )
  );

  fetchMPGSTransactionDetails$ = createEffect(() =>
    this.actions$.pipe(
      ofType(DashboardActions.fetchMPGSTransactionDetails),
      mergeMap(({ orderId }) => {
        return this.dashboardService.getMPGSTransactionDetails(orderId).pipe(
          map((details) =>
            DashboardActions.fetchMPGSTransactionDetailsSuccess({ details })
          ),
          catchError((error) => {
            return of(
              DashboardActions.fetchMPGSTransactionDetailsFailure({
                error: error.error?.description || 'An error occurred',
              })
            );
          })
        );
      })
    )
  );
}
