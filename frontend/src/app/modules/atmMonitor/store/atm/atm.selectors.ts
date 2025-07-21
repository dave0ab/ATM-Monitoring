import { createFeatureSelector, createSelector } from '@ngrx/store';
import {
  FetchAtmDetailsState,
  FetchAtmListState,
  FetchGatewayMonitortState,
  FetchReportStatusState,
  ExcelExportState,
} from '../../interfaces/atm.interface';
import { fetchGatewayMonitorFeatureKey } from './atm.reducer';

export const selectAtmState = createFeatureSelector<FetchAtmListState>('fAtm');
export const selectAtms = createSelector(
  selectAtmState,
  (state: FetchAtmListState) => state.atm
);

export const selectAtmTotalItems = createSelector(
  selectAtmState,
  (state: FetchAtmListState) => state.totalElements
);

export const selectAtmTotalPages = createSelector(
  selectAtmState,
  (state: FetchAtmListState) => state.totalPages
);
export const selectAtmLoading = createSelector(
  selectAtmState,
  (state: FetchAtmListState) => state.loading
);

export const selectAtmError = createSelector(
  selectAtmState,
  (state: FetchAtmListState) => state.error
);

export const selectAtmDetailState =
  createFeatureSelector<FetchAtmDetailsState>('dAtm');

export const selectAtmDetails = createSelector(
  selectAtmDetailState,
  (state) => state.atm
);
export const selectAtmDetailLoading = createSelector(
  selectAtmDetailState,
  (state) => state.loading
);
export const selectAtmDetailsError = createSelector(
  selectAtmDetailState,
  (state) => state.error
);

/// Gateway Monitor State

export const selectGatewayMonitorFeature =
  createFeatureSelector<FetchGatewayMonitortState>('gatewayMonitor');

export const selectGatewayMonitorData = createSelector(
  selectGatewayMonitorFeature,
  (state) => state?.gateway ?? []
);

export const selectGatewayMonitorLoading = createSelector(
  selectGatewayMonitorFeature,
  (state) => state?.loading ?? false
);

export const selectGatewayMonitorError = createSelector(
  selectGatewayMonitorFeature,
  (state) => state?.error ?? null
);

/// Report Status
export const selectReportStatusState =
  createFeatureSelector<FetchReportStatusState>('reportStatus');
export const selectReportStatus = createSelector(
  selectReportStatusState,
  (state: FetchReportStatusState) => state.reportData
);

export const selectReportStatusLoading = createSelector(
  selectReportStatusState,
  (state: FetchReportStatusState) => state.loading
);

export const selectReportStatusError = createSelector(
  selectReportStatusState,
  (state: FetchReportStatusState) => state.error
);

export const selectExcelExportState =
  createFeatureSelector<ExcelExportState>('excelExport');

export const selectExcelDownloadUrl = createSelector(
  selectExcelExportState,
  (state) => state?.downloadUrl ?? null
);

export const selectExcelExportLoading = createSelector(
  selectExcelExportState,
  (state) => state?.loading ?? false
);

export const selectExcelExportError = createSelector(
  selectExcelExportState,
  (state) => state?.error ?? null
);
