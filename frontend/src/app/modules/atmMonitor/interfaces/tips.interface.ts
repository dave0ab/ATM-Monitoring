export interface Tip {
  id: number;
  question: string;
  answer: string;
  votes: number;
  category: 'FAQ' | 'TIP' | 'SOLUTION';
  isOpen?: boolean;
  steps?: string[];
  icon?: string;
}

export interface TipFormData {
  question: string;
  answer: string;
  category: 'FAQ' | 'TIP' | 'SOLUTION';
  steps?: string[];
  icon?: string;
}
