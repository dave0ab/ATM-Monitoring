export interface MPGSTransaction {
  transactionId: string;
  transactionType: string;
  amount: number;
  currency: string;
  timeOfRecord: string;
  orderStatus: string;
  orderDescription?: string;
  orderId: string;
}
