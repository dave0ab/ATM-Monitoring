import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';

import { HttpClient, HttpParams } from '@angular/common/http';
import { environment } from 'src/environments/environment';
import {
  DashboardData,
  UserProfile,
  DashboardSummary,
  MPGSTransactionResponse,
  MPGSTransaction,
} from '../interfaces/Dashboard.intreface';

@Injectable({
  providedIn: 'root',
})
export class DashboardService {
  constructor(private http: HttpClient) {}

  getDashboard(): Observable<DashboardSummary> {
    return this.http.get<DashboardSummary>(
      `${environment.apiUrl}/dashboard/details`
    );
  }

  getUserProfile(): Observable<UserProfile> {
    return this.http.get<UserProfile>(
      `${environment.apiUrl}/dashboard/profile`
    );
  }

  getUserProfileOverview(userId: number): Observable<UserProfile> {
    return this.http.get<UserProfile>(
      `${environment.apiUrl}/auth/users/${userId}`
    );
  }

  getMPGSTransactions(
    page?: number,
    size?: number,
    orderStatus?: string,
    search?: string,
    brand?: string,
    startDate?: string,
    endDate?: string
  ): Observable<MPGSTransactionResponse> {
    let params = new HttpParams();

    if (page) {
      params = params.set('page', page);
    }
    if (size) {
      params = params.set('size', size);
    }
    if (orderStatus) {
      params = params.set('orderStatus', orderStatus);
    }
    if (search) {
      params = params.set('search', search);
    }
    if (brand) {
      params = params.set('brand', brand);
    }
    if (startDate) {
      params = params.set('startDate', startDate);
    }
    if (endDate) {
      params = params.set('endDate', endDate);
    }

    return this.http.get<MPGSTransactionResponse>(
      `${environment.apiUrl}/MPGS_Transactions`,
      { params }
    );
  }

  getMPGSTransactionDetails(orderId: string): Observable<MPGSTransaction[]> {
    return this.http.get<MPGSTransaction[]>(
      `${environment.apiUrl}/MPGS_Transactions/${orderId}`
    );
  }
}
