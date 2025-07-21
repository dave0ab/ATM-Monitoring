// src/app/services/fingerprint.service.ts
import { Injectable } from '@angular/core';
import FingerprintJS from '@fingerprintjs/fingerprintjs';

@Injectable({
  providedIn: 'root',
})
export class FingerprintService {
  private fingerprint: string | null = null;

  async getFingerprint(): Promise<string> {
    if (this.fingerprint) return this.fingerprint;

    const fp = await FingerprintJS.load();
    const result = await fp.get();
    this.fingerprint = result.visitorId;
    return this.fingerprint;
  }
}
