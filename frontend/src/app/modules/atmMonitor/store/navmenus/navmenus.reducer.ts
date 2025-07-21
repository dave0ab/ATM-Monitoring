import { createReducer, on } from '@ngrx/store';
import { TipsState } from '../../interfaces/navmenu.interface';
import * as TipsActions from './navmenus.actions';

export const initialTipsState: TipsState = {
  tips: [],
  loading: false,
  error: null,
};

// Define feature key
export const tipsFeatureKey = 'tips';

export const tipsReducer = createReducer(
  initialTipsState,

  // Load Tips
  on(TipsActions.loadTips, (state) => ({
    ...state,
    loading: true,
    error: null,
  })),
  on(TipsActions.loadTipsSuccess, (state, { tips }) => ({
    ...state,
    tips,
    loading: false,
    error: null,
  })),
  on(TipsActions.loadTipsFailure, (state, { error }) => ({
    ...state,
    loading: false,
    error,
  })),

  // Create Tip
  on(TipsActions.createTip, (state) => ({
    ...state,
    loading: true,
    error: null,
  })),
  on(TipsActions.createTipSuccess, (state, { tip }) => ({
    ...state,
    tips: [...state.tips, tip],
    loading: false,
    error: null,
  })),
  on(TipsActions.createTipFailure, (state, { error }) => ({
    ...state,
    loading: false,
    error,
  })),

  // Update Tip
  on(TipsActions.updateTip, (state) => ({
    ...state,
    loading: true,
    error: null,
  })),
  on(TipsActions.updateTipSuccess, (state, { tip }) => ({
    ...state,
    tips: state.tips.map((t) => (t.id === tip.id ? tip : t)),
    loading: false,
    error: null,
  })),
  on(TipsActions.updateTipFailure, (state, { error }) => ({
    ...state,
    loading: false,
    error,
  })),

  // Delete Tip
  on(TipsActions.deleteTip, (state) => ({
    ...state,
    loading: true,
    error: null,
  })),
  on(TipsActions.deleteTipSuccess, (state, { id }) => ({
    ...state,
    tips: state.tips.filter((tip) => tip.id !== id),
    loading: false,
    error: null,
  })),
  on(TipsActions.deleteTipFailure, (state, { error }) => ({
    ...state,
    loading: false,
    error,
  }))
);
