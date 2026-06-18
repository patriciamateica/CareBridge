import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { ClinicalLog } from '../models/clinicalLog';
import { buildApiUrl } from '../../api-config';
import { WsStompService } from './ws-stomp.service';

@Injectable({
  providedIn: 'root'
})
export class ClinicalLogService {
  private readonly apiUrl = buildApiUrl('/api/clinical-logs');

  constructor(private readonly http: HttpClient, private readonly ws: WsStompService) {}

  getAll(): Observable<any> {
    return this.http.get<any>(this.apiUrl);
  }

  getById(id: string): Observable<ClinicalLog> {
    return this.http.get<ClinicalLog>(`${this.apiUrl}/${id}`);
  }

  create(data: ClinicalLog): Observable<ClinicalLog> {
    return this.http.post<ClinicalLog>(this.apiUrl, data);
  }

  update(id: string, data: ClinicalLog): Observable<ClinicalLog> {
    return this.http.put<ClinicalLog>(`${this.apiUrl}/${id}`, data);
  }

  delete(id: string): Observable<boolean> {
    return this.http.delete<boolean>(`${this.apiUrl}/${id}`);
  }

  /** @deprecated Use WsStompService.listen() directly or via a facade service. */
  listenToUpdates(topic: string): Observable<any> {
    return this.ws.listen(topic);
  }
}
