export interface UserProfile {
  id: number;
  domainId: string;
  role: string;
  status: string;
  notificationLevel: string;
  createdAt: string;
  lastSignOn: string;
}

export interface DashboardProfile {
  id: number;
  domainId: string;
  role: string;
  status: string;
  notificationLevel: string;
  createdAt: string;
  lastSignOn: string;
}

export interface DashboardSummary {
  totalAtms: string;
  atmsOnline: string;
  atmsWithIssues: string;
  activeAlerts: string;
}

export interface DashboardData {
  id: string;
  location: string;
  status: string;
  lastUpdate: string;
  issueType: string;
  atmActivity: {
    month: string;
    value: number;
  }[];
  atmStatus: {
    label: string;
    value: number;
    color: string;
  }[];
}

export interface MPGSTransaction {
  transactionId: string;
  transactionType: string;
  orderDescription: string;
  timeOfRecord: string;
  orderId: string;
  cardNumber: string;
  cardExpiryMonth: string;
  cardExpiryYear: string;
  amount: number;
  currency: string;
  acquirer: string;
  acquirerCode: string;
  gatewayCode: string;
  gatewayRecommendation: string;
  cardholderName: string;
  issuer: string;
  brand: string;
  accountType: string;
  fundingMethod: string;
  authenticationStatus: string;
  threeDsVersion: string;
  firstName: string;
  lastName: string;
  email: string;
  orderStatus: string;
  ipAddress: string;
  merchant: string;
  merchantName: string;
  responseMessage: string;
}

export interface MPGSTransactionResponse {
  content: MPGSTransaction[];
  pageable: {
    pageNumber: number;
    pageSize: number;
    sort: {
      empty: boolean;
      unsorted: boolean;
      sorted: boolean;
    };
    offset: number;
    unpaged: boolean;
    paged: boolean;
  };
  last: boolean;
  totalPages: number;
  totalElements: number;
  first: boolean;
  numberOfElements: number;
  size: number;
  number: number;
  sort: {
    empty: boolean;
    unsorted: boolean;
    sorted: boolean;
  };
  empty: boolean;
}
