import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { PrimaryComponent } from 'src/app/modules/shared/components/buttons/primary.component';
import { Observable } from 'rxjs';
import { Store } from '@ngrx/store';
import { InputComponent } from 'src/app/modules/shared/components/inputs/input/input.component';
import { AuthService } from '../../services/auth.service';
import { HttpClientModule } from '@angular/common/http';
import { Router } from '@angular/router';
import { AlertErrorComponent } from 'src/app/modules/shared/components/alerts/alert-error/alert-error.component';
import { AlertSuccessComponent } from 'src/app/modules/shared/components/alerts/alert-success/alert-success.component';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { selectSessionExpired } from '../../store/auth.selectors';
import { AlertWarningComponent } from 'src/app/modules/shared/components/alerts/alert-warning/alert-warning.component';
import { clearSessionExpiredAction } from '../../store/auth.actions';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    PrimaryComponent,
    InputComponent,
    MatProgressBarModule,
    HttpClientModule,
    AlertWarningComponent,
    AlertErrorComponent,
  ],

  templateUrl: './login.component.html',
  styleUrls: ['./login.component.css'],
})
export class LoginComponent implements OnInit {
  form = this.fb.nonNullable.group({
    domain: ['', [Validators.required]],
    userPass: ['', [Validators.required, Validators.minLength(6)]],
  });
  public sessionError$: Observable<string | null> =
    this.store.select(selectSessionExpired);

  error: string = '';
  loading: boolean = false;
  constructor(
    private fb: FormBuilder,
    private auth: AuthService,
    private router: Router,
    private store: Store
  ) {}
  ngOnInit(): void {
    setTimeout(() => {
      this.store.dispatch(clearSessionExpiredAction());
    }, 3000);
  }
  onSubmit() {
    this.loading = true;
    const { domain, userPass } = this.form.getRawValue();
    this.auth.login(domain, userPass).subscribe({
      next: (res) => {
        this.router.navigateByUrl('/dashboard');
      },
      error: (err) => {
        this.loading = false;

        this.error = 'Bad Credentials';
      },
    });
    if (this.error) {
      setTimeout(() => {
        this.error = '';
      }, 3000);
    }
  }
}
