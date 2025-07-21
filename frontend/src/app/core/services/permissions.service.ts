import { Injectable } from '@angular/core';
import { ROLE_PERMISSIONS, UserRole } from '../config/permissions.config';
import { CryptoService } from './crypto.service';
import { AuthService } from 'src/app/modules/auth/services/auth.service';

@Injectable({
  providedIn: 'root',
})
export class PermissionsService {
  private userRole: UserRole | null = null;

  constructor(
    private cryptoService: CryptoService,
    private authService: AuthService
  ) {
    this.updateUserRole();
  }

  updateUserRole(): void {
    this.authService.getUserRole();
    const role = this.cryptoService.getRole();
    this.userRole = role ? (role as UserRole) : null;
  }

  hasMenuAccess(menuName: string): boolean {
    if (!this.userRole) return false;
    return (
      ROLE_PERMISSIONS[this.userRole]?.menus
        .map((m) => m.toLowerCase())
        .includes(menuName.toLowerCase()) ?? false
    );
  }

  hasRouteAccess(route: string): boolean {
    const role = this.cryptoService.getRole();
    if (!role) return false;

    const normalizedRoute = route.replace('/ATM', '').toLowerCase();
    const userRole = role as UserRole;

    return (
      ROLE_PERMISSIONS[userRole]?.routes
        .map((r) => r.toLowerCase())
        .includes(normalizedRoute) ?? false
    );
  }

  getAccessibleMenus(): string[] {
    if (!this.userRole) return [];
    return ROLE_PERMISSIONS[this.userRole]?.menus ?? [];
  }

  getAccessibleRoutes(): string[] {
    if (!this.userRole) return [];
    return ROLE_PERMISSIONS[this.userRole]?.routes ?? [];
  }
}
