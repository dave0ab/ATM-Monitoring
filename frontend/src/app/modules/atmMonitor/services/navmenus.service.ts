import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from 'src/environments/environment';
import { Tip } from '../interfaces/navmenu.interface';

@Injectable({
  providedIn: 'root',
})
export class TipsService {
  private apiUrl = environment.apiUrl + '/tips';

  constructor(private http: HttpClient) {}

  getTips(): Observable<Tip[]> {
    return this.http.get<Tip[]>(this.apiUrl);
  }

  createTip(tip: Omit<Tip, 'id' | 'createdDate'>): Observable<Tip> {
    return this.http.post<Tip>(this.apiUrl, tip);
  }

  updateTip(id: number, tip: Partial<Tip>): Observable<Tip> {
    return this.http.put<Tip>(`${this.apiUrl}/${id}`, tip);
  }

  deleteTip(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }
}
