import { DefaultState } from '../../shared/interfaces/default.interface';

export interface ForeClosureCreateState {
  foreclosureResponse: FetchForeclosureState[];
  loading: boolean;
  success: string | null;
  error: string | null;
}

export interface FetchForeclosureState {
  content: ForeClosureResponse[];
  loading: boolean;
  success: string | null;
  error: string | null;
  totalElements: number;
  totalPages: number;
}

export interface ForeClosure {
  id: number;

  borrowerName: string;

  debitAmtLsd: string;

  dateLsd: Date;

  collateralType: string;

  remark: string;

  foreClosureStatus: string;

  collateralDesc: string;

  process: string;
}

export interface ForeClosureResponse {
  id: number;

  borrowerName: string;

  debitAmtLsd: string;

  dateLsd: Date;

  collateralType: string;

  remark: string;

  foreClosureStatus: string;

  collateralDesc: string;

  process: string;
}

export interface FetchForeclosureState {
  pageable: {
    offset: number;
    sort: {
      empty: boolean;
      sorted: boolean;
      unsorted: boolean;
    };
    paged: boolean;
    unpaged: boolean;
    pageSize: number;
    pageNumber: number;
  };
  content: ForeClosureResponse[];
  totalElements: number;
  totalPages: number;
  number: number;
  size: number;
  first: boolean;
  last: boolean;
  numberOfElements: number;
  sort: {
    empty: boolean;
    sorted: boolean;
    unsorted: boolean;
  };
  loading: boolean;
  success: string | null;
  error: string | null;
}
