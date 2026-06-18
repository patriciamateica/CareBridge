import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { NurseDetails } from '../models/nurseDetails';
import { buildApiUrl } from '../../api-config';
import { WsStompService } from './ws-stomp.service';

@Injectable({
  providedIn: 'root'
})
export class NurseDetailsService {
  private apiUrl = buildApiUrl('/api/nurse-details');

  constructor(private http: HttpClient, private readonly ws: WsStompService) {}

  getAll(): Observable<any> {
    return this.http.get<any>(this.apiUrl);
  }

  getById(id: string): Observable<NurseDetails> {
    return this.http.get<NurseDetails>(`${this.apiUrl}/${id}`);
  }

  getByUserId(userId: string): Observable<NurseDetails> {
    return this.http.get<NurseDetails>(`${this.apiUrl}/by-user/${userId}`);
  }

  create(data: NurseDetails): Observable<NurseDetails> {
    return this.http.post<NurseDetails>(this.apiUrl, data);
  }

  update(id: string, data: NurseDetails): Observable<NurseDetails> {
    return this.http.put<NurseDetails>(`${this.apiUrl}/${id}`, data);
  }

  delete(id: string): Observable<boolean> {
    return this.http.delete<boolean>(`${this.apiUrl}/${id}`);
  }

  listenToUpdates(topic: string): Observable<any> {
    return this.ws.listen(topic);
  }
}
