import { DefaultState } from '../../shared/interfaces/default.interface';

export interface UserState extends DefaultState {}

export interface User {
  access_token: string;
  refresh_token: string;
  user: string;
}

export interface Users {
  id: number;
  domainId: string;
  role: 'ADMIN' | 'IT' | 'BRANCH' | 'OMNI_CHANNEL';
  status: 'ACTIVE' | 'INACTIVE';
  notificationLevel: String;
  phoneNumber: string;
  emailSubscribe: boolean;
}

export interface FetchUsersState {
  content: Users[];
  loading: boolean;
  success: string | null;
  error: string | null;
  totalPages: number;
  totalElements: number;
}

export interface RegisterUserRequest {
  domainId: string;
  role: string;
  notificationLevel: String;
  phoneNumber: string;
  emailSubscribe: boolean;
}

export interface RegisterUserResponse {
  message: string;
  domain_id: string;
  role: string;
  phoneNumber: string;
  error: boolean;
  emailSubscribe: boolean;
}

export interface UpdateUserRequest {
  domainId: string;
  role: string;
  status: string;
  notificationLevel: string;
  phoneNumber: string;
  emailSubscribe: boolean;
}

export interface UpdateUserResponse {
  id: number;
  domainId: string;
  role: string;
  status: string;
  notificationLevel: String;
  phoneNumber: string;
  emailSubscribe: boolean;
}
