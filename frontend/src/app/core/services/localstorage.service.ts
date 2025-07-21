import { Injectable } from '@angular/core';
import {
  USER_STORAGE_ATOKEN_KEY,
  USER_STORAGE,
} from 'src/app/modules/auth/services/auth.service';

@Injectable({
  providedIn: 'root',
})
export class LocalStorageService {
  constructor() {}

  public getAccessToken(): string | null {
    return localStorage.getItem(USER_STORAGE_ATOKEN_KEY);
  }

  public getUser(): string | null {
    return localStorage.getItem(USER_STORAGE);
  }
}
