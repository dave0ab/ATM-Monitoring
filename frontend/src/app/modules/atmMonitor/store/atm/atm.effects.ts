import { Injectable } from '@angular/core';
import { Actions, createEffect, ofType } from '@ngrx/effects';
import { of } from 'rxjs';
import { catchError, map, mergeMap, tap, switchMap } from 'rxjs/operators';

import * as AtmActions from './atm.actions';
import * as GatewayActions from './atm.actions';
import { AtmService } from '../../services/atm.service';
import { WebSocketService } from '../../services/web-socket.service';

@Injectable()
export class AtmEffects {
  constructor(
    private actions$: Actions,
    private atmServices: AtmService,
    private WebSocketService: WebSocketService
  ) {}

  fetchAtm$ = createEffect(() =>
    this.actions$.pipe(
      ofType(AtmActions.fetchAtm),
      mergeMap((action) =>
        this.atmServices
          .fetchAtm(
            action.pageNumber,
            action.pageSize,
            action.status,
            action.search,
            action.sort,
            action.sortBy,
            action.problem
          )
          .pipe(
            map((data) => {
              return AtmActions.fetchAtmSuccess({
                atm: data, // AtmResponse structure directly from the service
              });
            }),
            catchError((error) =>
              of(
                AtmActions.fetchAtmFailure({
                  error:
                    error.error?.message ||
                    'An error occurred while fetching ATMs',
                })
              )
            )
          )
      )
    )
  );

  fetchAtmDetails$ = createEffect(() =>
    this.actions$.pipe(
      ofType(AtmActions.fetchAtmDetails),
      mergeMap((action) =>
        this.atmServices.getAtmDetails(action.atmId).pipe(
          map((details) =>
            AtmActions.fetchAtmDetailsSuccess({
              atm: details,
            })
          ),
          catchError((error) =>
            of(
              AtmActions.fetchAtmDetailsFailure({
                error:
                  error.error?.message ||
                  'An error occurred while fetching ATM details',
              })
            )
          )
        )
      )
    )
  );

  listenForAtmUpdates$ = createEffect(() =>
    this.WebSocketService.getAtmUpdates().pipe(
      map((updatedAtm) =>
        AtmActions.updateAtmStatusSuccess({ atm: updatedAtm })
      )
    )
  );

  listenForGatewayUpdates$ = createEffect(() =>
    this.WebSocketService.getGatewayUpdates().pipe(
      map((updatedGateway) =>
        GatewayActions.updateGatewayStatusSuccess({ gateway: updatedGateway })
      )
    )
  );

  fetchGatewayMonitor$ = createEffect(() =>
    this.actions$.pipe(
      ofType(AtmActions.fetchGatewayMonitor),
      mergeMap(() =>
        this.atmServices.fetchGatewayMonitor().pipe(
          map((response) => {
            return AtmActions.fetchGatewayMonitorSuccess({ gateway: response });
          }),
          catchError((error) =>
            of(AtmActions.fetchGatewayMonitorFailure({ error: error.message }))
          )
        )
      )
    )
  );

  fetchStatusReports$ = createEffect(() =>
    this.actions$.pipe(
      ofType(AtmActions.fetchStatusReports),
      mergeMap((action) =>
        this.atmServices.fetchCurrnetStatusReport().pipe(
          map((data) => {
            return AtmActions.fetchStatusReportsSuccess({
              reportData: data,
            });
          }),
          catchError((error) =>
            of(
              AtmActions.fetchGatewayMonitorFailure({
                error:
                  error.error?.message ||
                  'An error occurred while fetching datas',
              })
            )
          )
        )
      )
    )
  );

  exportAtmExcel$ = createEffect(() =>
    this.actions$.pipe(
      ofType(AtmActions.exportAtmExcel),
      switchMap(({ exportType }) => {
        return this.atmServices.exportExcel(exportType).pipe(
          map((response) => {
            // Create a download link and trigger the download
            const link = document.createElement('a');
            link.href = response.downloadUrl;
            link.download = `${exportType}_${
              new Date().toISOString().split('T')[0]
            }.xlsx`;
            document.body.appendChild(link);
            link.click();
            document.body.removeChild(link);

            return AtmActions.exportAtmExcelSuccess({
              downloadUrl: response.downloadUrl,
            });
          }),
          catchError((error) =>
            of(AtmActions.exportAtmExcelFailure({ error: error.message }))
          )
        );
      })
    )
  );

  downloadExcel$ = createEffect(
    () =>
      this.actions$.pipe(
        ofType(AtmActions.exportAtmExcelSuccess),
        tap(({ downloadUrl }) => {
          window.location.href = downloadUrl;
        })
      ),
    { dispatch: false }
  );
}
