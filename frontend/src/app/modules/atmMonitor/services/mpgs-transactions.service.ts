import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { MPGSTransaction } from '../models/mpgs-transaction.model';
import { environment } from '../../../../environments/environment';

interface MPGSTransactionsResponse {
  transactions: MPGSTransaction[];
  totalItems: number;
}

@Injectable({
  providedIn: 'root',
})
export class MPGSTransactionsService {
  private apiUrl = `${environment.apiUrl}/mpgs-transactions`;

  constructor(private http: HttpClient) {}

  getTransactions(
    page: number,
    pageSize: number,
    status?: string,
    brand?: string,
    search?: string
  ): Observable<MPGSTransactionsResponse> {
    let params = new HttpParams()
      .set('page', page.toString())
      .set('pageSize', pageSize.toString());

    if (status) {
      params = params.set('status', status);
    }

    if (brand) {
      params = params.set('brand', brand);
    }

    if (search) {
      params = params.set('search', search);
    }

    return this.http.get<MPGSTransactionsResponse>(this.apiUrl, { params });
  }
}
