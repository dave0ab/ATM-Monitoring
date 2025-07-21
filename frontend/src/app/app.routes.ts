import { importProvidersFrom, inject } from '@angular/core';
import { Route } from '@angular/router';
import { AuthService } from './modules/auth/services/auth.service';
import { EffectsModule } from '@ngrx/effects';
import { DashboardEffects } from './modules/dashboard/store/dashboard.effects';
import { StoreModule } from '@ngrx/store';

import {
  authFeatureKey,
  authReducer,
  registrationFeatureKey,
  registrationReducer,
  updateUserFeatureKey,
  updateUserReducer,
  userFeatureKey,
  userReducer,
} from './modules/auth/store/auth.reducer';
import { AuthEffects } from './modules/auth/store/auth.effects';
import { AtmEffects } from './modules/atmMonitor/store/atm/atm.effects';
import {
  fetchAtmDetailFeatureKey,
  fetchAtmDetailReducer,
  fetchAtmFeatureKey,
  fetchAtmReducer,
  fetchGatewayMonitorFeatureKey,
  fetchGatewayMonitorReducer,
  fetchReportStatusFeatureKey,
  fetchReportStatusReducer,
  excelExportFeatureKey,
  excelExportReducer,
} from './modules/atmMonitor/store/atm/atm.reducer';
import {
  atmAccessReducer,
  atmAccessFeatureKey,
} from './modules/atmMonitor/store/access/access.reducer';
import { AtmAccessEffects } from './modules/atmMonitor/store/access/access.effects';
import { PermissionGuard } from './core/guards/permission.guard';
import {
  tipsFeatureKey,
  tipsReducer,
} from './modules/atmMonitor/store/navmenus/navmenus.reducer';
import { TipsEffects } from './modules/atmMonitor/store/navmenus/navmenus.effects';
import {
  dashboardDataFeatureKey,
  dashboardDataReducer,
  mpgsTransactionDetailsFeatureKey,
  mpgsTransactionDetailsReducer,
  mpgsTransactionsFeatureKey,
  mpgsTransactionsReducer,
  userProfileFeatureKey,
  userProfileReducer,
} from './modules/dashboard/store/dashboard.reducer';
import {
  locationFeatureKey,
  locationReducer,
} from './modules/atmMonitor/store/location/location.reducer';
import { LocationEffects } from './modules/atmMonitor/store/location/location.effects';

export const appRoutes: Route[] = [
  {
    path: '',
    loadComponent: () =>
      import('./modules/auth/pages/login/login.component').then(
        (mod) => mod.LoginComponent
      ),
    providers: [
      importProvidersFrom(EffectsModule.forFeature([AuthEffects])),

      importProvidersFrom(StoreModule.forFeature(authFeatureKey, authReducer)),
    ],
    canActivate: [() => inject(AuthService).shouldLogin()],
  },
  {
    path: 'dashboard',
    loadComponent: () =>
      import('./modules/dashboard/pages/dashboard/dashboard.component').then(
        (mod) => mod.DashboardComponent
      ),
    canActivate: [() => inject(AuthService).isLoggedIn()],
    children: [
      {
        path: '',
        loadComponent: () =>
          import('./modules/dashboard/pages/Defualt/defualt.component').then(
            (mod) => mod.DefualtComponent
          ),
      },

      {
        path: 'atm-list',
        canActivate: [() => inject(PermissionGuard).canActivateRoute()],
        loadComponent: () =>
          import('./modules/atmMonitor/pages/atmList/atm-list.component').then(
            (mod) => mod.AtmListComponent
          ),
        providers: [
          importProvidersFrom(
            StoreModule.forFeature(locationFeatureKey, locationReducer)
          ),
          importProvidersFrom(EffectsModule.forFeature([LocationEffects])),
        ],
      },

      // {
      //   path: 'reports',
      //   canActivate: [() => inject(PermissionGuard).canActivateRoute()],
      //   loadComponent: () =>
      //     import('./modules/atmMonitor/pages/reports/reports.component').then(
      //       (mod) => mod.ReportsComponent
      //     ),
      // },

      {
        path: 'gateway-monitor',
        canActivate: [() => inject(PermissionGuard).canActivateRoute()],
        loadComponent: () =>
          import(
            './modules/atmMonitor/pages/server-gateway-monitor/server-gateway-monitor.component'
          ).then((mod) => mod.ServerGatewayDashboardComponent),
      },
      {
        path: 'settings',
        canActivate: [() => inject(PermissionGuard).canActivateRoute()],
        loadComponent: () =>
          import('./modules/atmMonitor/pages/settings/settings.component').then(
            (mod) => mod.SettingsComponent
          ),
      },
      {
        path: 'MPGS-transactions',
        canActivate: [() => inject(PermissionGuard).canActivateRoute()],
        loadComponent: () =>
          import(
            './modules/atmMonitor/pages/MPGS-Transactions/mpgs-transactions.component'
          ).then((mod) => mod.MPGSTransactionsComponent),
      },
      {
        path: 'tips',
        canActivate: [() => inject(PermissionGuard).canActivateRoute()],
        loadComponent: () =>
          import(
            './modules/atmMonitor/pages/Tips-Solutions/tips-solutions.component'
          ).then((mod) => mod.TipsSolutionsComponent),
      },
      {
        path: 'user-details/:id',
        canActivate: [() => inject(PermissionGuard).canActivateRoute()],
        loadComponent: () =>
          import(
            './modules/atmMonitor/pages/user-details/user-details.component'
          ).then((mod) => mod.UserDetailsComponent),
        providers: [
          importProvidersFrom(
            StoreModule.forFeature(atmAccessFeatureKey, atmAccessReducer),
            StoreModule.forFeature(fetchAtmFeatureKey, fetchAtmReducer)
          ),
          importProvidersFrom(
            EffectsModule.forFeature([
              AtmEffects,
              AtmAccessEffects,
              DashboardEffects,
              LocationEffects,
            ])
          ),
        ],
      },
    ],
    providers: [
      importProvidersFrom(
        EffectsModule.forFeature([
          DashboardEffects,
          AtmEffects,
          AtmAccessEffects,
          TipsEffects,
          LocationEffects,
        ])
      ),
      importProvidersFrom(
        StoreModule.forFeature(fetchAtmDetailFeatureKey, fetchAtmDetailReducer),
        StoreModule.forFeature(userFeatureKey, userReducer),
        StoreModule.forFeature(registrationFeatureKey, registrationReducer),
        StoreModule.forFeature(updateUserFeatureKey, updateUserReducer),
        StoreModule.forFeature(fetchAtmFeatureKey, fetchAtmReducer),
        StoreModule.forFeature(atmAccessFeatureKey, atmAccessReducer),
        StoreModule.forFeature(
          fetchReportStatusFeatureKey,
          fetchReportStatusReducer
        ),
        StoreModule.forFeature(tipsFeatureKey, tipsReducer),
        StoreModule.forFeature(
          fetchGatewayMonitorFeatureKey,
          fetchGatewayMonitorReducer
        ),
        StoreModule.forFeature(excelExportFeatureKey, excelExportReducer),
        StoreModule.forFeature(dashboardDataFeatureKey, dashboardDataReducer),
        StoreModule.forFeature(userProfileFeatureKey, userProfileReducer),
        StoreModule.forFeature(locationFeatureKey, locationReducer),
        StoreModule.forFeature(
          mpgsTransactionDetailsFeatureKey,
          mpgsTransactionDetailsReducer
        ),
        StoreModule.forFeature(
          mpgsTransactionsFeatureKey,
          mpgsTransactionsReducer
        )
      ),
    ],
  },

  {
    path: '**',
    loadComponent: () =>
      import('./modules/dashboard/pages/error-page/error-page.component').then(
        (mod) => mod.ErrorPageComponent
      ),
  },
];
