export interface Tip {
  id: number;
  category: 'FAQ' | 'SOLUTIONS' | 'TIPS';
  title: string;
  details: string;
  createdDate: string;
  roles: string[];
  isOpen?: boolean;
}

export interface TipsState {
  tips: Tip[];
  loading: boolean;
  error: string | null;
}
