import { Injectable } from '@angular/core';
import { Actions, createEffect, ofType } from '@ngrx/effects';
import { of } from 'rxjs';
import { map, mergeMap, catchError } from 'rxjs/operators';
import { AtmService } from '../../services/atm.service';
import * as LocationActions from './location.actions';

@Injectable()
export class LocationEffects {
  getLocation$ = createEffect(() =>
    this.actions$.pipe(
      ofType(LocationActions.getLocation),
      mergeMap(({ tid }) =>
        this.atmService.getAtmLocation(tid).pipe(
          map((location) => LocationActions.getLocationSuccess({ location })),
          catchError((error) =>
            of(LocationActions.getLocationFailure({ error: error.message }))
          )
        )
      )
    )
  );

  updateLocation$ = createEffect(() =>
    this.actions$.pipe(
      ofType(LocationActions.updateLocation),
      mergeMap(({ location }) =>
        this.atmService.addOrUpdateLocation(location).pipe(
          map((updatedLocation) =>
            LocationActions.updateLocationSuccess({ location: updatedLocation })
          ),
          catchError((error) =>
            of(LocationActions.updateLocationFailure({ error: error.message }))
          )
        )
      )
    )
  );

  constructor(private actions$: Actions, private atmService: AtmService) {}
}
