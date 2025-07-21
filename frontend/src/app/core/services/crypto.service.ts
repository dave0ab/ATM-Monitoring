import { Injectable } from '@angular/core';
import * as CryptoJS from 'crypto-js';
import { HttpClient } from '@angular/common/http';
import { UserRole } from '../config/permissions.config';

@Injectable({
  providedIn: 'root',
})
export class CryptoService {
  private readonly SECRET_KEY = 'dave0abtopsecret';
  private readonly ROLE_KEY = 'enc_role';

  constructor(private http: HttpClient) {}

  encrypt(text: string): string {
    return CryptoJS.AES.encrypt(
      JSON.stringify({ data: text, timestamp: new Date().getTime() }),
      this.SECRET_KEY
    ).toString();
  }

  decrypt(encryptedText: string): string | null {
    try {
      const bytes = CryptoJS.AES.decrypt(encryptedText, this.SECRET_KEY);
      const decrypted = JSON.parse(bytes.toString(CryptoJS.enc.Utf8));

      // Check if token is not older than 24 hours
      if (new Date().getTime() - decrypted.timestamp < 24 * 60 * 60 * 1000) {
        return decrypted.data;
      }
      return null;
    } catch {
      return null;
    }
  }

  setRole(role: string): void {
    const encrypted = this.encrypt(role);
    sessionStorage.setItem(this.ROLE_KEY, encrypted);
  }

  getRole(): string | null {
    const encrypted = sessionStorage.getItem(this.ROLE_KEY);
    if (!encrypted) return null;

    return this.decrypt(encrypted);
  }

  clearRole(): void {
    sessionStorage.removeItem(this.ROLE_KEY);
  }
}
