import { createAction, props } from '@ngrx/store';
import {
  AtmResponse,
  Atm,
  ServerGateway,
  AtmCurrentStatus,
} from '../../interfaces/atm.interface';

// Action to initiate fetching ATMs with pagination and filters
export const fetchAtm = createAction(
  '[Request] Fetch Atm List',
  props<{
    pageNumber: number;
    pageSize: number;
    status: string | null;
    search: string | null;
    sort: string | null;
    sortBy: string | null;
    problem: string | null;
  }>()
);

// Action triggered on successfully fetching ATMs
export const fetchAtmSuccess = createAction(
  '[Request] Fetch Atm Success',
  props<{
    atm: AtmResponse;
  }>()
);

// Action triggered on failure to fetch ATMs
export const fetchAtmFailure = createAction(
  '[Request] Fetch Atm Failure',
  props<{ error: string }>()
);

// Action to clear ATM fetch results
export const clearFetchAtm = createAction('[Request] Clear Atm');

// Action to initiate fetching a single ATM's details by foreclosure ID
export const fetchAtmDetails = createAction(
  '[Atm] Fetch Atm Details',
  props<{ atmId: number }>()
);

// Action triggered on successfully fetching ATM details
export const fetchAtmDetailsSuccess = createAction(
  '[Atm] Fetch Atm Details Success',
  props<{ atm: Atm }>()
);

// Action triggered on failure to fetch ATM details
export const fetchAtmDetailsFailure = createAction(
  '[Atm] Fetch Atm Details Failure',
  props<{ error: string }>()
);

export const updateAtmStatusSuccess = createAction(
  '[WebSocket] Update ATM Status Success',
  props<{ atm: Atm }>()
);

export const updateAtmStatusFailure = createAction(
  '[WebSocket] Update ATM Status Failure',
  props<{ error: string }>()
);

////// Gateway Monitor

export const fetchGatewayMonitor = createAction('[Gateway Monitor] Fetch List');

// Action triggered on successfully fetching ATMs
export const fetchGatewayMonitorSuccess = createAction(
  '[Request] Fetch data Success',
  props<{
    gateway: ServerGateway;
  }>()
);

// Action triggered on failure to fetch ATMs
export const fetchGatewayMonitorFailure = createAction(
  '[Request] Fetch data Failure',
  props<{ error: string }>()
);

// Action to clear ATM fetch results
export const clearFetchGatewayMonitor = createAction('[Request] Clear data');

export const updateGatewayStatusSuccess = createAction(
  '[WebSocket] Update ATM Status Success',
  props<{ gateway: ServerGateway }>()
);

export const updateGatewayStatusFailure = createAction(
  '[WebSocket] Update ATM Status Failure',
  props<{ error: string }>()
);

////// REPORT

export const fetchStatusReports = createAction('[Gateway Monitor] Fetch List');

// Action triggered on successfully fetching ATMs
export const fetchStatusReportsSuccess = createAction(
  '[Request] Fetch data Success',
  props<{
    reportData: AtmCurrentStatus;
  }>()
);

// Action triggered on failure to fetch
export const fetchStatusReportsFailure = createAction(
  '[Request] Fetch data Failure',
  props<{ error: string }>()
);

// Action to clear  fetch results
export const clearFetchStatusReports = createAction('[Request] Clear data');

export const updateFetchStatusReportsSuccess = createAction(
  '[WebSocket] Update  Status Success',
  props<{ report: AtmCurrentStatus }>()
);

export const updateFetchStatusReportsFailure = createAction(
  '[WebSocket] Update  Status Failure',
  props<{ error: string }>()
);

// Excel Export Actions
export const exportAtmExcel = createAction(
  '[ATM] Export Excel',
  props<{ exportType: string }>()
);

export const exportAtmExcelSuccess = createAction(
  '[ATM] Export Excel Success',
  props<{ downloadUrl: string }>()
);

export const exportAtmExcelFailure = createAction(
  '[ATM] Export Excel Failure',
  props<{ error: string }>()
);
