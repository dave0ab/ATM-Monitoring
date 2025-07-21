import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { HttpClient, HttpParams } from '@angular/common/http';
import { environment } from 'src/environments/environment';
import {
  Atm,
  AtmCurrentStatus,
  AtmResponse,
  FetchAtmDetailsState,
  FetchAtmListState,
  ServerGateway,
  ExcelExportResponse,
} from '../interfaces/atm.interface';
import { AtmAccess } from '../interfaces/atm-access.interface';
import { AtmLocation } from '../interfaces/atm-location.interface';

@Injectable({
  providedIn: 'root',
})
export class AtmService {
  getAtms() {
    throw new Error('Method not implemented.');
  }
  constructor(private http: HttpClient) {}

  public fetchAtm(
    pageNumber: number,
    pageSize: number,
    status: string | null,
    search: string | null,
    sort: string | null,
    sortBy: string | null,
    problem: string | null
  ): Observable<AtmResponse> {
    let params = new HttpParams()
      .set('pageNumber', pageNumber.toString())
      .set('pageSize', pageSize.toString());

    if (status) params = params.set('status', status);
    if (search) params = params.set('search', search);
    if (sort) params = params.set('sort', sort);
    if (sortBy) params = params.set('sortBy', sortBy);
    if (problem) params = params.set('problem', problem);

    return this.http.get<AtmResponse>(`${environment.apiUrl}/atms`, {
      params,
    });
  }

  public getAtmDetails(atmId: number): Observable<Atm> {
    return this.http.get<Atm>(`${environment.apiUrl}/atms/${atmId}`);
  }

  public fetchGatewayMonitor(): Observable<ServerGateway> {
    return this.http.get<ServerGateway>(`${environment.apiUrl}/gateways`, {});
  }

  public fetchCurrnetStatusReport(): Observable<AtmCurrentStatus> {
    return this.http.get<AtmCurrentStatus>(
      `${environment.apiUrl}/atms/currentStatus`,
      {}
    );
  }

  public saveAtmAccess(atmAccesses: AtmAccess[]): Observable<AtmAccess[]> {
    return this.http.post<AtmAccess[]>(
      `${environment.apiUrl}/atm-access`,
      atmAccesses
    );
  }

  fetchAtmAccess(userId: string): Observable<AtmAccess[]> {
    return this.http.get<AtmAccess[]>(
      `${environment.apiUrl}/atm-access/user/${userId}`
    );
  }

  exportExcel(type: string): Observable<ExcelExportResponse> {
    return this.http.get<ExcelExportResponse>(
      `${environment.apiUrl}/atms/excelExport/${type}`
    );
  }

  getAtmLocation(tid: string): Observable<AtmLocation> {
    return this.http.get<AtmLocation>(
      `${environment.apiUrl}/settings/get/${tid}`
    );
  }

  addOrUpdateLocation(location: { tid: string; la: string; lo: string }) {
    return this.http.post<AtmLocation>(
      `${environment.apiUrl}/settings/addOrUpdateLocation`,
      location
    );
  }

  public updateRemark(atmId: string, remark: string): Observable<string> {
    const payload = {
      atmId: atmId,
      remark: remark,
    };

    return this.http.post<string>(
      `${environment.apiUrl}/atms/addRemark`,
      payload,
      { responseType: 'text' as 'json' } // Because the API returns plain text
    );
  }
}
