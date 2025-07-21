import { createAction, props } from '@ngrx/store';
import { AtmLocation } from '../../interfaces/atm-location.interface';

// Get Location
export const getLocation = createAction(
  '[Location] Get Location',
  props<{ tid: string }>()
);

export const getLocationSuccess = createAction(
  '[Location] Get Location Success',
  props<{ location: AtmLocation }>()
);

export const getLocationFailure = createAction(
  '[Location] Get Location Failure',
  props<{ error: string }>()
);

// Add/Update Location
export const updateLocation = createAction(
  '[Location] Update Location',
  props<{ location: { tid: string; la: string; lo: string } }>()
);

export const updateLocationSuccess = createAction(
  '[Location] Update Location Success',
  props<{ location: AtmLocation }>()
);

export const updateLocationFailure = createAction(
  '[Location] Update Location Failure',
  props<{ error: string }>()
);
