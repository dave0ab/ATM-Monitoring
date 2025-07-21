import { Component, Inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import {
  MatDialogRef,
  MAT_DIALOG_DATA,
  MatDialogModule,
} from '@angular/material/dialog';
import {
  FormBuilder,
  FormGroup,
  Validators,
  ReactiveFormsModule,
} from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';

@Component({
  selector: 'app-location-edit-dialog',
  standalone: true,
  imports: [
    CommonModule,
    MatDialogModule,
    MatButtonModule,
    MatFormFieldModule,
    MatInputModule,
    ReactiveFormsModule,
  ],
  template: `
    <div class="p-6">
      <h2 class="text-xl font-semibold mb-4">Edit Location</h2>
      <form [formGroup]="locationForm" (ngSubmit)="onSubmit()">
        <div class="space-y-4">
          <mat-form-field appearance="outline" class="w-full">
            <mat-label>Latitude</mat-label>
            <input
              matInput
              formControlName="latitude"
              type="number"
              step="0.000001"
            />
          </mat-form-field>

          <mat-form-field appearance="outline" class="w-full">
            <mat-label>Longitude</mat-label>
            <input
              matInput
              formControlName="longitude"
              type="number"
              step="0.000001"
            />
          </mat-form-field>
        </div>

        <div class="flex justify-end space-x-3 mt-6">
          <button
            mat-button
            type="button"
            (click)="onCancel()"
            class="px-4 py-2 text-gray-700 hover:bg-gray-100 rounded-lg"
          >
            Cancel
          </button>
          <button
            mat-raised-button
            color="primary"
            type="submit"
            [disabled]="locationForm.invalid"
            class="px-4 py-2 bg-rose-600 text-white hover:bg-rose-700 rounded-lg"
          >
            Save
          </button>
        </div>
      </form>
    </div>
  `,
})
export class LocationEditDialogComponent {
  locationForm: FormGroup;

  constructor(
    private dialogRef: MatDialogRef<LocationEditDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public data: any,
    private fb: FormBuilder
  ) {
    this.locationForm = this.fb.group({
      latitude: [data?.latitude || '', [Validators.required]],
      longitude: [data?.longitude || '', [Validators.required]],
    });
  }

  onSubmit() {
    if (this.locationForm.valid) {
      this.dialogRef.close(this.locationForm.value);
    }
  }

  onCancel() {
    this.dialogRef.close();
  }
}
