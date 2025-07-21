import { Injectable, inject } from '@angular/core';
import { Router, NavigationEnd } from '@angular/router';
import { PermissionsService } from '../services/permissions.service';
import { AuthService } from 'src/app/modules/auth/services/auth.service';
import { filter } from 'rxjs/operators';

@Injectable({
  providedIn: 'root',
})
export class PermissionGuard {
  private router = inject(Router);
  private permissionsService = inject(PermissionsService);
  private readonly ROUTE_KEY = 'currentRoute';

  constructor(private auth: AuthService) {
    // Listen to successful navigation events
    this.router.events
      .pipe(filter((event) => event instanceof NavigationEnd))
      .subscribe((event: any) => {
        // Save successful navigation routes
        if (!event.url.includes('/login')) {
          localStorage.setItem(this.ROUTE_KEY, event.url);
        }
      });
  }

  async canActivateRoute(): Promise<boolean> {
    // On refresh, use the actual URL instead of router.url
    const currentUrl = window.location.pathname.replace('/ATM', '');
    const hasAccess = await this.permissionsService.hasRouteAccess(currentUrl);

    if (hasAccess) {
      return true;
    }

    const a = this.auth.getUserRole().subscribe();
    const savedRoute = localStorage.getItem(this.ROUTE_KEY);

    if (savedRoute) {
      this.router.navigate([savedRoute]);
    } else {
      this.router.navigate(['/dashboard']);
    }

    return false;
  }
}
