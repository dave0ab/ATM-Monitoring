export type UserRole =
  | 'ADMIN'
  | 'IT'
  | 'OMNI_CHANNEL'
  | 'BRANCH'
  | 'OMNI_CHANNEL_RECONCILIATION';

export const ROLE_PERMISSIONS: Record<
  UserRole,
  { menus: string[]; routes: string[] }
> = {
  ADMIN: {
    menus: [
      'Dashboard',
      'ATM List',
      'Gateway Monitor',
      'Reports',
      'MPGS Transactions',
      'Settings',
      'Tips',
    ],
    routes: [
      '/dashboard',
      '/dashboard/atm-list',
      '/dashboard/gateway-monitor',
      '/dashboard/MPGS-transactions',
      '/dashboard/user-details',
      '/dashboard/tips',
      '/dashboard/settings',
      '/dashboard/reports',
    ],
  },
  IT: {
    menus: [
      'Dashboard',
      'ATM List',
      'Gateway Monitor',
      'MPGS Transactions',
      'Tips',
    ],
    routes: [
      '/dashboard',
      '/dashboard/atm-list',
      '/dashboard/gateway-monitor',
      '/dashboard/MPGS-transactions',
      '/dashboard/tips',
    ],
  },
  OMNI_CHANNEL: {
    menus: ['Dashboard', 'ATM List', 'Tips'],
    routes: ['/dashboard', '/dashboard/atm-list', '/dashboard/tips'],
  },
  OMNI_CHANNEL_RECONCILIATION: {
    menus: ['Dashboard', 'MPGS Transactions'],
    routes: ['/dashboard', '/dashboard/MPGS-transactions'],
  },

  BRANCH: {
    menus: ['Dashboard', 'ATM List', 'Reports', 'Tips'],
    routes: [
      '/dashboard',
      '/dashboard/atm-list',
      '/dashboard/tips',
      '/dashboard/reports',
    ],
  },
};
