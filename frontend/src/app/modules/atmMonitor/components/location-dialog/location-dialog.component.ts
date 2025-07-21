import { Component, Inject, OnInit, OnDestroy } from '@angular/core';

import {
  MatDialogRef,
  MAT_DIALOG_DATA,
  MatDialogModule,
} from '@angular/material/dialog';
import { Store } from '@ngrx/store';
import { MatButtonModule } from '@angular/material/button';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { DomSanitizer, SafeResourceUrl } from '@angular/platform-browser';
import * as LocationActions from '../../store/location/location.actions';
import * as LocationSelectors from '../../store/location/location.selectors';
import { MatIconModule } from '@angular/material/icon';
import {
  FormBuilder,
  FormGroup,
  Validators,
  ReactiveFormsModule,
} from '@angular/forms';
import { Subject } from 'rxjs';
import { takeUntil } from 'rxjs/operators';
import { ToastService } from '../../services/toast.service';
import { selectLocationState } from '../../store/location/location.selectors';

interface LocationDialogData {
  atmId: string;
  atmName: string;
  mapLink?: string;
}

@Component({
  selector: 'app-location-dialog',
  standalone: true,
  imports: [
    MatDialogModule,
    MatButtonModule,
    MatProgressSpinnerModule,
    MatIconModule,
    ReactiveFormsModule
],
  template: `
    <div class="p-6">
      <div class="flex justify-between items-center mb-6">
        <h2 class="text-xl font-semibold text-gray-800">
          ATM Location - {{ data.atmName }}
        </h2>
        <button mat-icon-button (click)="close()">
          <mat-icon>close</mat-icon>
        </button>
      </div>
    
      <div class="bg-gray-50 p-4 rounded mb-4">
        <div class="grid grid-cols-2 gap-4">
          <div>
            <span class="text-gray-600 text-sm">ATM ID</span>
            <p class="font-medium">{{ data.atmId }}</p>
          </div>
          <div>
            <span class="text-gray-600 text-sm">Location Status</span>
            <p class="font-medium">
              {{ data.mapLink ? 'Mapped' : 'Not Mapped' }}
            </p>
          </div>
        </div>
      </div>
    
      <div class="rounded-lg overflow-hidden border border-gray-200 mb-4">
        <div class="bg-gray-100 h-[400px]">
          @if (data.mapLink) {
            <iframe
              [src]="mapUrl"
              width="100%"
              height="100%"
              frameborder="0"
              allowfullscreen
              (load)="onIframeLoad()"
              (error)="onIframeError($event)"
            ></iframe>
          }
          @if (!data.mapLink) {
            <div
              class="flex items-center justify-center h-full text-gray-500"
              >
              No Location Data Available
            </div>
          }
        </div>
      </div>
    
      <form [formGroup]="locationForm" class="mb-4">
        <div class="grid grid-cols-2 gap-4">
          <div>
            <label class="block text-sm font-medium text-gray-700 mb-1"
              >Latitude</label
              >
              <input
                type="number"
                formControlName="latitude"
                class="w-full p-2 border rounded-md"
                placeholder="Enter latitude"
                />
              </div>
              <div>
                <label class="block text-sm font-medium text-gray-700 mb-1"
                  >Longitude</label
                  >
                  <input
                    type="number"
                    formControlName="longitude"
                    class="w-full p-2 border rounded-md"
                    placeholder="Enter longitude"
                    />
                  </div>
                </div>
              </form>
    
              <div class="flex justify-end space-x-3">
                <button mat-button (click)="close()">Close</button>
                <button
                  mat-raised-button
                  color="primary"
                  (click)="updateLocation()"
                  [disabled]="!locationForm.valid"
                  >
                  Update Location
                </button>
              </div>
            </div>
    `,
})
export class LocationDialogComponent implements OnInit, OnDestroy {
  locationForm: FormGroup;
  debugInfo: string = '';
  private destroy$ = new Subject<void>();
  mapUrl: SafeResourceUrl;
  private iframeLoaded = false;

  constructor(
    private dialogRef: MatDialogRef<LocationDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public data: LocationDialogData,
    private store: Store,
    private sanitizer: DomSanitizer,
    private fb: FormBuilder,
    private toastService: ToastService
  ) {
    this.debugInfo = `Data received: ${JSON.stringify(data, null, 2)}`;

    const coords = this.extractCoordinates(data.mapLink);
    this.locationForm = this.fb.group({
      latitude: [coords?.lat || '', [Validators.required]],
      longitude: [coords?.lng || '', [Validators.required]],
    });
    this.mapUrl = this.getSafeUrl(data.mapLink || '');

    // Add subscription for location state
    this.store.select(selectLocationState).subscribe((state) => {
      if (state.error) {
        this.toastService.showError(
          'Failed to update location: ' + state.error
        );
      }
      if (!state.loading && !state.error && state.location) {
        this.toastService.showSuccess('Location updated successfully');
        this.dialogRef.close(true); // Pass true to indicate successful update
      }
    });
  }

  private extractCoordinates(mapLink?: string) {
    if (!mapLink) return null;
    const match = mapLink.match(/q=(-?\d+\.?\d*),(-?\d+\.?\d*)/);
    return match ? { lat: match[1], lng: match[2] } : null;
  }

  private getSafeUrl(url: string): SafeResourceUrl {
    return this.sanitizer.bypassSecurityTrustResourceUrl(url);
  }

  ngOnInit() {
    console.log('');
  }

  onIframeLoad() {
    if (!this.iframeLoaded) {
      this.iframeLoaded = true;
    }
  }

  onIframeError(error: any) {
    this.debugInfo += `\nIframe error: ${JSON.stringify(error)}`;
  }

  close() {
    this.dialogRef.close();
  }

  updateLocation() {
    if (this.locationForm.valid) {
      const { latitude, longitude } = this.locationForm.value;
      this.store.dispatch(
        LocationActions.updateLocation({
          location: {
            tid: this.data.atmId,
            la: latitude.toString(),
            lo: longitude.toString(),
          },
        })
      );
    }
  }

  ngOnDestroy() {
    this.destroy$.next();
    this.destroy$.complete();
  }
}
