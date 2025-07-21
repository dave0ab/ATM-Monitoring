import { ApplicationConfig, importProvidersFrom } from '@angular/core';
import {
  PreloadAllModules,
  provideRouter,
  withEnabledBlockingInitialNavigation,
  withPreloading,
} from '@angular/router';
import { appRoutes } from './app.routes';
import {
  HTTP_INTERCEPTORS,
  provideHttpClient,
  withInterceptorsFromDi,
} from '@angular/common/http';
import { EffectsModule, provideEffects } from '@ngrx/effects';
import {
  routerReducer,
  provideRouterStore,
  StoreRouterConnectingModule,
} from '@ngrx/router-store';
import { StoreModule, provideStore } from '@ngrx/store';
import {
  StoreDevtoolsModule,
  provideStoreDevtools,
} from '@ngrx/store-devtools';
import { AuthInterceptor } from './core/interceptors/authentication.interceptor';
import {
  provideAnimations,
  provideNoopAnimations,
} from '@angular/platform-browser/animations';
import { provideToastr } from 'ngx-toastr';
import { NgxDaterangepickerMd } from 'ngx-daterangepicker-material';
export const appConfig: ApplicationConfig = {
  providers: [
    provideEffects(),
    provideStore(),
    provideRouter(appRoutes, withPreloading(PreloadAllModules)),
    provideHttpClient(withInterceptorsFromDi()),
    provideNoopAnimations(),
    importProvidersFrom(StoreModule.forRoot()),
    importProvidersFrom(StoreRouterConnectingModule.forRoot()),
    importProvidersFrom(
      StoreDevtoolsModule.instrument({ connectInZone: true })
    ),
    importProvidersFrom(EffectsModule.forRoot()),
    importProvidersFrom([NgxDaterangepickerMd.forRoot({})]),
    provideAnimations(),
    provideToastr(),
    { provide: HTTP_INTERCEPTORS, useClass: AuthInterceptor, multi: true },
  ],
};
