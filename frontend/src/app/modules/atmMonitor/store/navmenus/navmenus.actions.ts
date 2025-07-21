import { createAction, props } from '@ngrx/store';
import { Tip } from '../../interfaces/navmenu.interface';

// Load Tips
export const loadTips = createAction('[Tips] Load Tips');
export const loadTipsSuccess = createAction(
  '[Tips] Load Tips Success',
  props<{ tips: Tip[] }>()
);
export const loadTipsFailure = createAction(
  '[Tips] Load Tips Failure',
  props<{ error: string }>()
);

// Create Tip
export const createTip = createAction(
  '[Tips] Create Tip',
  props<{ tip: Omit<Tip, 'id' | 'createdDate'> }>()
);
export const createTipSuccess = createAction(
  '[Tips] Create Tip Success',
  props<{ tip: Tip }>()
);
export const createTipFailure = createAction(
  '[Tips] Create Tip Failure',
  props<{ error: string }>()
);

// Update Tip
export const updateTip = createAction(
  '[Tips] Update Tip',
  props<{ id: number; tip: Partial<Tip> }>()
);
export const updateTipSuccess = createAction(
  '[Tips] Update Tip Success',
  props<{ tip: Tip }>()
);
export const updateTipFailure = createAction(
  '[Tips] Update Tip Failure',
  props<{ error: string }>()
);

// Delete Tip
export const deleteTip = createAction(
  '[Tips] Delete Tip',
  props<{ id: number }>()
);
export const deleteTipSuccess = createAction(
  '[Tips] Delete Tip Success',
  props<{ id: number }>()
);
export const deleteTipFailure = createAction(
  '[Tips] Delete Tip Failure',
  props<{ error: string }>()
);
