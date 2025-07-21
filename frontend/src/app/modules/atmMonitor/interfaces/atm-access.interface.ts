export interface AtmAccess {
  id?: number;
  terminalId: string;
  userId: string;
  status: 'CHECKED' | 'UNCHECKED';
  createdAt?: string;
}

export interface AtmAccessState {
  atmAccesses: AtmAccess[];
  loading: boolean;
  error: string | null;
  success: string | null;
}
