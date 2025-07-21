// dashboard.component.ts
import { Component, HostListener, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import {
  ActivatedRoute,
  NavigationEnd,
  Router,
  RouterModule,
} from '@angular/router';
import { FormsModule } from '@angular/forms';
import { AuthService } from 'src/app/modules/auth/services/auth.service';
import { Store } from '@ngrx/store';
import { LocalStorageService } from 'src/app/core/services/localstorage.service';
import { PermissionsService } from 'src/app/core/services/permissions.service';
import { ProfileDialogComponent } from 'src/app/modules/shared/components/dialogs/profile-dialog.component';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import {
  selectDashboardProfile,
  selectDashboardProfileLoading,
  selectDashboardProfileError,
  selectUserProfile,
  selectUserProfileLoading,
  selectUserProfileError,
  selectUserProfileData,
} from 'src/app/modules/dashboard/store/dashboard.selectors';
import { take } from 'rxjs/operators';
import {
  fetchDashboardProfile,
  fetchUserProfile,
} from '../../store/dashboard.actions';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule, RouterModule, FormsModule, MatDialogModule],
  templateUrl: './dashboard.component.html',
})
export class DashboardComponent implements OnInit {
  isMobileSidebarHidden: any;

  toggleMobileSidebar() {
    this.isMobileSidebarHidden = !this.isMobileSidebarHidden;
  }
  toggleMobileSearch() {
    this.showMobileSearch = !this.showMobileSearch;
  }
  searchQuery: string = '';

  isMobile: any;
  showMobileSearch: boolean = false;

  onSearch() {
    this.router.navigate(['/dashboard/atm-list'], {
      queryParams: { search: this.searchQuery },
      queryParamsHandling: 'merge', // keeps existing query params, if any
    });
  }

  isSidebarCollapsed = false;
  showNotifications = false;
  showUserMenu = false;
  activeRoute: string = '';

  profile$ = this.store.select(selectDashboardProfile);
  profileLoading$ = this.store.select(selectDashboardProfileLoading);
  profileError$ = this.store.select(selectDashboardProfileError);

  userProfile$ = this.store.select(selectUserProfileData);
  userProfileLoading$ = this.store.select(selectUserProfileLoading);
  userProfileError$ = this.store.select(selectUserProfileError);

  constructor(
    private router: Router,
    private route: ActivatedRoute,
    private auth: AuthService,
    private store: Store,
    public localStorageServices: LocalStorageService,
    private authService: AuthService,
    private permissionsService: PermissionsService,
    private dialog: MatDialog
  ) {}

  navItems = [
    {
      label: 'Dashboard',
      route: '/dashboard',
      iconViewBox: '0 0 24 24',
      iconPath:
        'M3 12l2-2m0 0l7-7 7 7M5 10v10a1 1 0 001 1h3m10-11l2 2m-2-2v10a1 1 0 01-1 1h-3m-6 0a1 1 0 001-1v-4a1 1 0 011-1h2a1 1 0 011 1v4a1 1 0 001 1m-6 0h6',
    },
    {
      label: 'ATM List',
      route: '/dashboard/atm-list',
      iconViewBox: '0 0 24 24',
      iconPath: 'M4 6h16M4 10h16M4 14h16M4 18h16',
    },
    {
      label: 'Gateway Monitor',
      route: '/dashboard/gateway-monitor',
      iconViewBox: '0 0 24 24',
      iconPath:
        'M3 5h18a1 1 0 0 1 1 1v12a1 1 0 0 1-1 1H3a1 1 0 0 1-1-1V6a1 1 0 0 1 1-1zm1 3v2h14V8H4zm0 4v2h14v-2H4zm0 4v2h14v-2H4zm16-4a1 1 0 0 0 1-1V9a1 1 0 0 0-1-1h-2v6h2a1 1 0 0 0 1-1z',
    },
    {
      label: 'MPGS Transactions',
      route: '/dashboard/MPGS-transactions',
      iconViewBox: '0 0 24 24',
      iconPath:
        'M2 4h20a2 2 0 0 1 2 2v2H0V6a2 2 0 0 1 2-2zm0 6h22v10a2 2 0 0 1-2 2H2a2 2 0 0 1-2-2V10zm4 4a1 1 0 0 0 0 2h4a1 1 0 1 0 0-2H6z',
    },

    // {
    //   label: 'Reports',
    //   route: '/dashboard/reports',
    //   iconViewBox: '0 0 24 24',
    //   iconPath:
    //     'M9 17v-2m3 2v-4m3 4v-6m2 10H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z',
    // },
    {
      label: 'Settings',
      route: '/dashboard/settings',
      iconViewBox: '0 0 24 24',
      iconPath:
        'M10.325 4.317c.426-1.756 2.924-1.756 3.35 0a1.724 1.724 0 002.573 1.066c1.543-.94 3.31.826 2.37 2.37a1.724 1.724 0 001.065 2.572c1.756.426 1.756 2.924 0 3.35a1.724 1.724 0 00-1.066 2.573c.94 1.543-.826 3.31-2.37 2.37a1.724 1.724 0 00-2.572 1.065c-.426 1.756-2.924 1.756-3.35 0a1.724 1.724 0 00-2.573-1.066c-1.543.94-3.31-.826-2.37-2.37a1.724 1.724 0 00-1.065-2.572c-1.756-.426-1.756-2.924 0-3.35a1.724 1.724 0 001.066-2.573c-.94-1.543.826-3.31 2.37-2.37.996.608 2.296.07 2.572-1.065z',
    },
    {
      label: 'Tips',
      route: '/dashboard/tips',
      iconViewBox: '0 0 24 24',
      iconPath:
        'M12 2a10 10 0 100 20 10 10 0 000-20zm0 18a8 8 0 110-16 8 8 0 010 16zm.9-4.1h-1.8a.9.9 0 010-1.8h1.8a.9.9 0 010 1.8zm-.9-3.9c-.59 0-1.07-.46-1.07-1.03v-.34c0-.51.4-.92.91-.92.51 0 .91.41.91.92v.23c0 .36-.28.64-.63.64zm-.2-7a.9.9 0 00-.7 1.1c.11.43.52.72.94.66.49-.06.86-.47.86-.96A.9.9 0 0012 5.1z',
    },
  ];

  summaryCards = [
    {
      label: 'Total ATMs',
      value: '1,234',
      iconPath:
        'M17 9V7a2 2 0 00-2-2H5a2 2 0 00-2 2v6a2 2 0 002 2h2m2 4h10a2 2 0  002-2v-6a2 2 0 00-2-2H9a2 2 0 00-2 2v6a2 2 0 002 2zm7-5a2 2 0 11-4 0 2 2 0 014 0z',
      iconClass: 'bg-blue-100 text-blue-600',
    },
    {
      label: 'ATMs Online',
      value: '1,180',
      iconPath: 'M5 13l4 4L19 7',
      iconClass: 'bg-green-100 text-green-600',
    },
    {
      label: 'ATMs with Issues',
      value: '54',
      iconPath:
        'M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z',
      iconClass: 'bg-yellow-100 text-yellow-600',
    },
    {
      label: 'Active Alerts',
      value: '23',
      iconPath:
        'M15 17h5l-1.405-1.405A2.032 2.032 0 0118 14.158V11a6.002 6.002 0 00-4-5.659V5a2 2 0 10-4 0v.341C7.67 6.165 6 8.388 6 11v3.159c0 .538-.214 1.055-.595 1.436L4 17h5m6 0v1a3 3 0 11-6 0v-1m6 0H9',
      iconClass: 'bg-red-100 text-red-600',
    },
  ];

  isActive(route: string): boolean {
    return this.router.url === route;
  }

  toggleSidebar() {
    this.isSidebarCollapsed = !this.isSidebarCollapsed;
  }

  toggleNotifications() {
    this.showNotifications = !this.showNotifications;
    this.showUserMenu = false;
  }

  toggleUserMenu() {
    this.showUserMenu = !this.showUserMenu;
    this.showNotifications = false;
  }

  ngOnInit(): void {
    this.router.events.subscribe((event) => {
      if (event instanceof NavigationEnd) {
        this.activeRoute = event.urlAfterRedirects;
        this.permissionsService.updateUserRole();
      }
    });

    this.permissionsService.updateUserRole();

    const a = this.authService.getUserRole().subscribe();

    this.store.dispatch(fetchUserProfile());
    this.permissionsService.updateUserRole();
  }

  @HostListener('window:resize', ['$event'])
  onResize(event: Event): void {
    this.checkMobileView();
    this.toggleSidebar(); // Check on resize
  }

  private checkMobileView(): void {
    this.isMobileSidebarHidden = window.innerWidth < 768;
  }

  isRouteActive(route: string): boolean {
    return this.activeRoute === route;
  }

  logout() {
    this.authService.logOut();
    this.router.navigateByUrl('/');
  }

  get filteredNavItems() {
    let retryCount = 0;
    const maxRetries = 3;
    const retryDelay = 5; // 1 second

    this.permissionsService.updateUserRole();

    const tryGetItems = (): any[] => {
      const items = this.navItems.filter((item) =>
        this.permissionsService.hasMenuAccess(item.label)
      );

      if (items.length === 0 && retryCount < maxRetries) {
        retryCount++;
        setTimeout(() => {
          this.permissionsService.updateUserRole();
          tryGetItems();
        }, retryDelay);
      }

      return items;
    };

    return tryGetItems();
  }

  openProfile(): void {
    // First dispatch
    this.store.dispatch(fetchUserProfile());

    // Subscribe to check data with take(1) to avoid memory leaks
    this.userProfile$.pipe(take(1)).subscribe({
      next: (profile) => {
        console.log('Opening dialog with profile:', profile);

        const dialogRef = this.dialog.open(ProfileDialogComponent, {
          width: '600px',
          data: {
            domainId: profile?.domainId || 'N/A',
            role: profile?.role || 'User',
            status: profile?.status || 'Active',
            notificationLevel: profile?.notificationLevel || 'Normal',
            createdAt: profile?.createdAt || new Date(),
            lastSignOn: profile?.lastSignOn || new Date(),
          },
        });

        dialogRef.afterClosed().subscribe(() => {
          console.log('Dialog closed');
        });
      },
      error: (error) => console.error('Error opening profile:', error),
    });
  }
}
