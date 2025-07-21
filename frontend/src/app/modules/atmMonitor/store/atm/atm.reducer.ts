import { createReducer, on } from '@ngrx/store';
import * as AtmActions from './atm.actions';
import {
  FetchAtmListState,
  FetchAtmDetailsState,
  FetchGatewayMonitortState,
  FetchReportStatusState,
  ExcelExportState,
} from '../../interfaces/atm.interface';

export const initialFetchAtmState: FetchAtmListState = {
  atm: null,
  loading: false,
  success: null,
  error: null,
  totalElements: 0,
  totalPages: 0,
};

export const initialFetchDetailAtmState: FetchAtmDetailsState = {
  atm: null,
  loading: false,
  success: null,
  error: null,
};

export const fetchAtmFeatureKey = 'fAtm';

export const fetchAtmReducer = createReducer(
  initialFetchAtmState,
  on(AtmActions.fetchAtm, (state) => ({
    ...state,
    loading: true,
    error: null,
  })),
  on(AtmActions.fetchAtmSuccess, (state, action) => ({
    ...state,
    atm: action.atm, // Update 'atm' to hold the full response
    loading: false,
    error: null,
    totalElements: action.atm.totalElements,
    totalPages: action.atm.totalPages,
  })),
  on(AtmActions.fetchAtmFailure, (state, { error }) => ({
    ...state,
    loading: false,
    error,
  })),
  on(AtmActions.clearFetchAtm, (state) => ({
    ...state,
    loading: false,
    error: null,
    atm: null,
    totalElements: 0,
    totalPages: 0,
  }))
);

export const fetchAtmDetailFeatureKey = 'dAtm';

export const fetchAtmDetailReducer = createReducer(
  initialFetchDetailAtmState,
  on(AtmActions.fetchAtmDetails, (state) => ({
    ...state,
    loading: true,
    error: null,
  })),
  on(AtmActions.fetchAtmDetailsSuccess, (state, { atm }) => ({
    ...state,
    atm,
    loading: false,
    error: null,
  })),
  on(AtmActions.fetchAtmDetailsFailure, (state, { error }) => ({
    ...state,
    loading: false,
    error,
  }))
);

//Gateway Monitor Reducer

export const initialState: FetchGatewayMonitortState = {
  gateway: [],
  loading: false,
  success: null,
  error: null,
};

export const fetchGatewayMonitorFeatureKey = 'gatewayMonitor';

export const fetchGatewayMonitorReducer = createReducer(
  initialState,
  on(AtmActions.fetchGatewayMonitor, (state) => ({
    ...state,
    loading: true,
    error: null,
  })),
  on(AtmActions.fetchGatewayMonitorSuccess, (state, { gateway }) => {
    const validGateways = Array.isArray(gateway)
      ? gateway.filter((g) => g !== null && g !== undefined)
      : gateway
      ? [gateway].filter((g) => g !== null && g !== undefined)
      : [];

    return {
      ...state,
      gateway: validGateways.length > 0 ? validGateways : state.gateway, // Keep existing data if new data is empty
      loading: false,
    };
  }),
  on(AtmActions.fetchGatewayMonitorFailure, (state, { error }) => ({
    ...state,
    error,
    loading: false,
  }))
);

//Report Status Monitor

export const initialFetchReportStatusState: FetchReportStatusState = {
  reportData: null,
  loading: false,
  success: null,
  error: null,
};

export const fetchReportStatusFeatureKey = 'reportStatus';

export const fetchReportStatusReducer = createReducer(
  initialFetchReportStatusState,
  on(AtmActions.fetchStatusReports, (state) => ({
    ...state,
    loading: true,
    error: null,
  })),
  on(AtmActions.fetchStatusReportsSuccess, (state, action) => ({
    ...state,
    reportData: action.reportData,
    loading: false,
    error: null,
  })),
  on(AtmActions.fetchStatusReportsFailure, (state, { error }) => ({
    ...state,
    loading: false,
    error,
  })),
  on(AtmActions.clearFetchStatusReports, (state) => ({
    ...state,
    loading: false,
    error: null,
    atm: null,
  }))
);

// Excel Export Reducer

export const initialExcelExportState: ExcelExportState = {
  downloadUrl: null,
  loading: false,
  error: null,
  success: null,
};

export const excelExportFeatureKey = 'excelExport';

export const excelExportReducer = createReducer(
  initialExcelExportState,
  on(AtmActions.exportAtmExcel, (state) => ({
    ...state,
    loading: true,
    error: null,
    success: null,
  })),
  on(AtmActions.exportAtmExcelSuccess, (state, { downloadUrl }) => ({
    ...state,
    downloadUrl,
    loading: false,
    error: null,
    success: 'Excel export generated successfully',
  })),
  on(AtmActions.exportAtmExcelFailure, (state, { error }) => ({
    ...state,
    loading: false,
    error,
    downloadUrl: null,
    success: null,
  }))
);
