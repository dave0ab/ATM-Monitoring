import { Injectable } from '@angular/core';
import { Actions, createEffect, ofType } from '@ngrx/effects';
import { of } from 'rxjs';
import { map, mergeMap, catchError } from 'rxjs/operators';
import { TipsService } from '../../services/navmenus.service';
import * as TipsActions from './navmenus.actions';

@Injectable()
export class TipsEffects {
  loadTips$ = createEffect(() =>
    this.actions$.pipe(
      ofType(TipsActions.loadTips),
      mergeMap(() =>
        this.tipsService.getTips().pipe(
          map((tips) => TipsActions.loadTipsSuccess({ tips })),
          catchError((error) =>
            of(TipsActions.loadTipsFailure({ error: error.message }))
          )
        )
      )
    )
  );

  createTip$ = createEffect(() =>
    this.actions$.pipe(
      ofType(TipsActions.createTip),
      mergeMap(({ tip }) =>
        this.tipsService.createTip(tip).pipe(
          map((newTip) => TipsActions.createTipSuccess({ tip: newTip })),
          catchError((error) =>
            of(TipsActions.createTipFailure({ error: error.message }))
          )
        )
      )
    )
  );

  updateTip$ = createEffect(() =>
    this.actions$.pipe(
      ofType(TipsActions.updateTip),
      mergeMap(({ id, tip }) =>
        this.tipsService.updateTip(id, tip).pipe(
          map((updatedTip) =>
            TipsActions.updateTipSuccess({ tip: updatedTip })
          ),
          catchError((error) =>
            of(TipsActions.updateTipFailure({ error: error.message }))
          )
        )
      )
    )
  );

  deleteTip$ = createEffect(() =>
    this.actions$.pipe(
      ofType(TipsActions.deleteTip),
      mergeMap(({ id }) =>
        this.tipsService.deleteTip(id).pipe(
          map(() => TipsActions.deleteTipSuccess({ id })),
          catchError((error) =>
            of(TipsActions.deleteTipFailure({ error: error.message }))
          )
        )
      )
    )
  );

  constructor(private actions$: Actions, private tipsService: TipsService) {}
}
