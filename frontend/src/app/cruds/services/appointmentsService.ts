import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { Appointments } from '../models/appointments';
import { catchError } from 'rxjs/operators';

@Injectable({
  providedIn: 'root'
})
export class AppointmentsService {
  private apiUrl = 'http://localhost:8080/api/appointments';

  constructor(private http: HttpClient) {}

  getAll(): Observable<any> {
    return this.http.get<any>(this.apiUrl).pipe(
      catchError(err => {
        console.error('Error fetching all appointments:', err);
        return throwError(() => err);
      })
    );
  }

  getById(id: string): Observable<Appointments> {
    return this.http.get<Appointments>(`${this.apiUrl}/${id}`).pipe(
      catchError(err => {
        console.error('Error fetching appointment:', err);
        return throwError(() => err);
      })
    );
  }

  getByPatientId(patientId: string, page = 0, size = 5): Observable<any> {
    return this.http.get<any>(`${this.apiUrl}/patient/${patientId}?page=${page}&size=${size}`).pipe(
      catchError(err => {
        console.error('Error fetching appointments for patient:', err);
        return throwError(() => err);
      })
    );
  }

  getByNurseId(nurseId: string, page = 0, size = 5): Observable<any> {
    return this.http.get<any>(`${this.apiUrl}/nurse/${nurseId}?page=${page}&size=${size}`).pipe(
      catchError(err => {
        console.error('Error fetching appointments for nurse:', err);
        return throwError(() => err);
      })
    );
  }

  create(data: Appointments): Observable<Appointments> {
    return this.http.post<Appointments>(this.apiUrl, data).pipe(
      catchError(err => {
        console.error('Error creating appointment:', err);
        return throwError(() => err);
      })
    );
  }

  update(id: string, data: Appointments): Observable<Appointments> {
    return this.http.put<Appointments>(`${this.apiUrl}/${id}`, data).pipe(
      catchError(err => {
        console.error('Error updating appointment:', err);
        return throwError(() => err);
      })
    );
  }

  delete(id: string): Observable<boolean> {
    return this.http.delete<boolean>(`${this.apiUrl}/${id}`).pipe(
      catchError(err => {
        console.error('Error deleting appointment:', err);
        return throwError(() => err);
      })
    );
  }
}
