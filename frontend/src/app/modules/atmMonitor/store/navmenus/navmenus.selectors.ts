import { createFeatureSelector, createSelector } from '@ngrx/store';
import { Tip } from '../../interfaces/tips.interface';
import { TipsState } from '../../interfaces/navmenu.interface';
import { tipsFeatureKey } from './navmenus.reducer';

export const selectTipsState = createFeatureSelector<TipsState>(tipsFeatureKey);

export const selectAllTips = createSelector(
  selectTipsState,
  (state) => state.tips
);

export const selectTipsLoading = createSelector(
  selectTipsState,
  (state) => state.loading
);

export const selectTipsError = createSelector(
  selectTipsState,
  (state) => state.error
);
