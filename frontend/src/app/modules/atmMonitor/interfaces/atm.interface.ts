import { DefaultState } from '../../shared/interfaces/default.interface';

export interface FetchAtmListState {
  atm: AtmResponse | null;
  loading: boolean;
  success: string | null;
  error: string | null;
  totalPages: number | null;
  totalElements: number | null;
}

export interface FetchAtmDetailsState {
  atm: Atm | null;
  loading: boolean;
  success: string | null;
  error: string | null;
}

export interface Atm {
  atmId: string;
  atmName: string;
  modelDesc: string;
  address1: string | null;
  region: number;
  regionName: string;
  requiredOpState: number;
  actualOpState: number;
  actualOpStateDesc: string;
  commsState: number;
  commStateDesc: string;
  status: number;
  stateName: string;
  device: number;
  deviceName: string;
  failure: number;
  failureName: string;
  device2: number;
  device2Name: string;
  failure2: number;
  failure2Name: string;
  successTxnLastUpdate: string;
  lastFs: string;
  lastDs: string;
  atmIdNum: string;
  atmGroupId: number;
  cashTxnLastUpdate: string | null;
  branchName: string;
  lastUpdated: string;
  lastUpdatedEpoch: number;
  atmIP: string;
  pingStatus: string;
  [key: string]: any;
  mapLink: string;
  cashAdd: string;
  cashLeft: string;
  remark: string;
}

export interface Pageable {
  pageNumber: number;
  pageSize: number;
  offset: number;
  paged: boolean;
  unpaged: boolean;
}

export interface Sort {
  sorted: boolean;
  unsorted: boolean;
  empty: boolean;
}

export interface AtmResponse {
  content: Atm[];
  pageable: Pageable;
  last: boolean;
  totalPages: number;
  totalElements: number;
  size: number;
  number: number;
  sort: Sort;
  first: boolean;
  numberOfElements: number;
  empty: boolean;
}

/// gateway monitor

export interface FetchGatewayMonitortState {
  gateway: ServerGateway[];
  loading: boolean;
  success: string | null;
  error: string | null;
}

export interface ServerGateway {
  systemId: string;
  serverName: string;
  actualStat: string | null;
  locDesStat: string | null;
  remDesStat: string | null;
  commsStatus: string | null;
  serverAddr: string;
  serverPort: string | null;
  network: string;
  actualUpTime: number | null;
  actualDownTime: number | null;
  actualResetTime: number | null;
  lineStat: string | null;
  lineChangeTime: string | null;
  actualChangeTime: string | null;
  lastIncEchoTime: string | null;
  remDesSon: string | null;
  uptimePercentage: number;
}

export interface AtmCurrentStatus {
  terminalId: string;
  atmName: string;
  status: string;
  id: number;
}

export interface FetchReportStatusState {
  reportData: AtmCurrentStatus | null;
  loading: boolean;
  success: string | null;
  error: string | null;
}

export interface ExcelExportState {
  downloadUrl: string | null;
  loading: boolean;
  error: string | null;
  success: string | null;
}

export interface ExcelExportResponse {
  downloadUrl: string;
}
