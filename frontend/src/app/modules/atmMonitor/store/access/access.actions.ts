import { createAction, props } from '@ngrx/store';
import { AtmAccess } from '../../interfaces/atm-access.interface';

export const saveAtmAccess = createAction(
  '[ATM Access] Save ATM Access',
  props<{ atmAccesses: AtmAccess[] }>()
);

export const saveAtmAccessSuccess = createAction(
  '[ATM Access] Save ATM Access Success',
  props<{ atmAccesses: AtmAccess[] }>()
);

export const saveAtmAccessFailure = createAction(
  '[ATM Access] Save ATM Access Failure',
  props<{ error: string }>()
);

export const fetchAtmAccess = createAction(
  '[ATM Access] Fetch ATM Access',
  props<{ userId: string }>()
);

export const fetchAtmAccessSuccess = createAction(
  '[ATM Access] Fetch ATM Access Success',
  props<{ atmAccesses: AtmAccess[] }>()
);

export const fetchAtmAccessFailure = createAction(
  '[ATM Access] Fetch ATM Access Failure',
  props<{ error: string }>()
);
