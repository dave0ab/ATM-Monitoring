import { Component, OnInit, AfterViewInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Store } from '@ngrx/store';
import {
  selectDashboardData,
  selectMPGSTransactions,
  selectMPGSTransactionsLoading,
  selectMPGSTransactionsError,
} from '../../store/dashboard.selectors';
import * as DashboardActions from '../../store/dashboard.actions';
import Chart from 'chart.js/auto';
import { DashboardSummary } from '../../interfaces/Dashboard.intreface';
import { Subject, takeUntil } from 'rxjs';
import { Router } from '@angular/router';

interface SummaryCard {
  label: string;
  value: string;
  iconPath: string;
  iconClass: string;
}

interface ATM {
  id: string;
  location: string;
  status: string;
  lastUpdate: string;
  issueType: string;
}

@Component({
  selector: 'app-defualt',
  templateUrl: './defualt.component.html',
  styleUrls: ['./defualt.component.scss'],
  standalone: true,
  imports: [CommonModule],
})
export class DefualtComponent implements OnInit, AfterViewInit, OnDestroy {
  private destroy$ = new Subject<void>();
  private pieChart: Chart<'doughnut', number[], string> | null = null;
  private lineChart: Chart<'line', number[], string> | null = null;

  // Add MPGS transactions state
  mpgsTransactions$ = this.store.select(selectMPGSTransactions);
  mpgsLoading$ = this.store.select(selectMPGSTransactionsLoading);
  mpgsError$ = this.store.select(selectMPGSTransactionsError);

  summaryCards: SummaryCard[] = [
    {
      label: 'Total ATMs',
      value: '5',
      iconPath:
        'M17 9V7a2 2 0 00-2-2H5a2 2 0 00-2 2v6a2 2 0 002 2h2m2 4h10a2 2 0  002-2v-6a2 2 0 00-2-2H9a2 2 0 00-2 2v6a2 2 0 002 2zm7-5a2 2 0 11-4 0 2 2 0 014 0z',
      iconClass: 'bg-blue-100 text-blue-600',
    },
    {
      label: 'ATMs Online',
      value: '1',
      iconPath: 'M5 13l4 4L19 7',
      iconClass: 'bg-green-100 text-green-600',
    },
    {
      label: 'ATMs with Issues',
      value: '4',
      iconPath:
        'M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z',
      iconClass: 'bg-yellow-100 text-yellow-600',
    },
    {
      label: 'Active Alerts',
      value: '4',
      iconPath:
        'M15 17h5l-1.405-1.405A2.032 2.032 0 0118 14.158V11a6.002 6.002 0 00-4-5.659V5a2 2 0 10-4 0v.341C7.67 6.165 6 8.388 6 11v3.159c0 .538-.214 1.055-.595 1.436L4 17h5m6 0v1a3 3 0 11-6 0v-1m6 0H9',
      iconClass: 'bg-red-100 text-red-600',
    },
  ];

  atmTableHeaders: string[] = [
    'ID',
    'Location',
    'Status',
    'Last Update',
    'Issue Type',
    'Actions',
  ];

  atmList: ATM[] = [
    {
      id: 'ATM001',
      location: 'Main St, City A',
      status: 'Online',
      lastUpdate: '2023-04-10 14:30',
      issueType: '-',
    },
    {
      id: 'ATM002',
      location: 'Central Ave, City B',
      status: 'Offline',
      lastUpdate: '2023-04-10 13:45',
      issueType: 'Network Error',
    },
    {
      id: 'ATM003',
      location: 'Park Rd, City C',
      status: 'Warning',
      lastUpdate: '2023-04-10 15:00',
      issueType: 'Low Cash',
    },
  ];

  atmActivityData = [
    { month: 'Jan', value: 65 },
    { month: 'Feb', value: 59 },
    { month: 'Mar', value: 80 },
    { month: 'Apr', value: 81 },
    { month: 'May', value: 56 },
    { month: 'Jun', value: 55 },
  ];

  atmStatusData = [
    { label: 'Online', value: 60, color: '#10B981' },
    { label: 'Offline', value: 30, color: '#EF4444' },
    { label: 'Maintenance', value: 10, color: '#F59E0B' },
  ];
  private intervalId: any;

  constructor(private store: Store, private router: Router) {}

  ngOnInit() {
    // Fetch dashboard data
    this.store.dispatch(DashboardActions.fetchDashboardData());

    // Subscribe to dashboard data
    this.store
      .select(selectDashboardData)
      .pipe(takeUntil(this.destroy$))
      .subscribe((data) => {
        if (data) {
          this.updateSummaryCards(data);
          this.updateCharts(data);
        }
      });

    // Set up interval for dashboard data refresh
    this.intervalId = setInterval(() => {
      this.store.dispatch(DashboardActions.fetchDashboardData());
    }, 60000);
  }

  private updateSummaryCards(data: DashboardSummary) {
    this.summaryCards = [
      {
        label: 'Total ATMs',
        value: data.totalAtms,
        iconPath:
          'M17 9V7a2 2 0 00-2-2H5a2 2 0 00-2 2v6a2 2 0 002 2h2m2 4h10a2 2 0  002-2v-6a2 2 0 00-2-2H9a2 2 0 00-2 2v6a2 2 0 002 2zm7-5a2 2 0 11-4 0 2 2 0 014 0z',
        iconClass: 'bg-blue-100 text-blue-600',
      },
      {
        label: 'ATMs Online',
        value: data.atmsOnline,
        iconPath: 'M5 13l4 4L19 7',
        iconClass: 'bg-green-100 text-green-600',
      },
      {
        label: 'ATMs with Issues',
        value: data.atmsWithIssues,
        iconPath:
          'M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z',
        iconClass: 'bg-yellow-100 text-yellow-600',
      },
      {
        label: 'Active Alerts',
        value: data.activeAlerts,
        iconPath:
          'M15 17h5l-1.405-1.405A2.032 2.032 0 0118 14.158V11a6.002 6.002 0 00-4-5.659V5a2 2 0 10-4 0v.341C7.67 6.165 6 8.388 6 11v3.159c0 .538-.214 1.055-.595 1.436L4 17h5m6 0v1a3 3 0 11-6 0v-1m6 0H9',
        iconClass: 'bg-red-100 text-red-600',
      },
    ];
  }

  ngAfterViewInit() {
    // Delay chart initialization to ensure DOM is ready
    setTimeout(() => {
      this.initializeCharts();
    }, 0);
  }

  ngOnDestroy() {
    // Cleanup charts and subscriptions
    if (this.pieChart) {
      this.pieChart.destroy();
    }
    if (this.lineChart) {
      this.lineChart.destroy();
    }
    this.destroy$.next();
    this.destroy$.complete();

    if (this.intervalId) {
      clearInterval(this.intervalId);
    }
  }

  private initializeCharts() {
    try {
      this.renderPieChart();
      this.renderLineChart();
    } catch (error) {
      console.error('Error initializing charts:', error);
    }
  }
  onCardClick() {
    this.router.navigate(['/dashboard/atm-list']);
  }

  private updateCharts(data: DashboardSummary) {
    try {
      const onlineAtms = parseInt(data.atmsOnline);
      const issuesAtms = parseInt(data.atmsWithIssues);

      if (this.pieChart) {
        this.pieChart.data.datasets[0].data = [onlineAtms, issuesAtms];
        this.pieChart.update('none'); // Use 'none' mode for better performance
      }

      if (this.lineChart) {
        // Update line chart data
        this.lineChart.update('none');
      }
    } catch (error) {
      console.error('Error updating charts:', error);
    }
  }

  private renderPieChart() {
    const ctx = document.getElementById('pieChart') as HTMLCanvasElement;
    if (!ctx) return;

    if (this.pieChart) {
      this.pieChart.destroy();
    }

    this.pieChart = new Chart(ctx, {
      type: 'doughnut',
      data: {
        labels: ['Online ATMs', 'ATMs with Issues'],
        datasets: [
          {
            data: [
              parseInt(this.summaryCards[1].value),
              parseInt(this.summaryCards[2].value),
            ],
            backgroundColor: ['#10B981', '#EF4444'],
            borderColor: ['#065F46', '#991B1B'],
            borderWidth: 1,
            hoverOffset: 4,
          },
        ],
      },
      options: {
        responsive: true,
        plugins: {
          legend: {
            position: 'bottom',
            labels: {
              padding: 20,
              font: {
                size: 12,
              },
            },
          },
          title: {
            display: true,
            text: 'ATM Status Distribution',
            font: {
              size: 16,
            },
          },
        },
        cutout: '60%',
      },
    });
  }

  private renderLineChart() {
    const ctx = document.getElementById('lineChart') as HTMLCanvasElement;
    if (!ctx) return;

    if (this.lineChart) {
      this.lineChart.destroy();
    }

    // Last 6 months data
    const currentDate = new Date();
    const months = Array.from({ length: 6 }, (_, i) => {
      const d = new Date(currentDate);
      d.setMonth(d.getMonth() - (5 - i));
      return d.toLocaleString('default', { month: 'short' });
    });

    // Calculate random historical data based on current ATM counts
    const totalAtms = parseInt(this.summaryCards[0].value);
    const historicalData = months.map(() => {
      const onlineCount = Math.floor(
        Math.random() * (totalAtms * 0.8) + totalAtms * 0.2
      );
      return {
        online: onlineCount,
        issues: totalAtms - onlineCount,
      };
    });

    this.lineChart = new Chart(ctx, {
      type: 'line',
      data: {
        labels: months,
        datasets: [
          {
            label: 'Online ATMs',
            data: historicalData.map((d) => d.online),
            borderColor: '#10B981',
            backgroundColor: 'rgba(16, 185, 129, 0.1)',
            fill: true,
            tension: 0.4,
            borderWidth: 2,
          },
          {
            label: 'ATMs with Issues',
            data: historicalData.map((d) => d.issues),
            borderColor: '#EF4444',
            backgroundColor: 'rgba(239, 68, 68, 0.1)',
            fill: true,
            tension: 0.4,
            borderWidth: 2,
          },
        ],
      },
      options: {
        responsive: true,
        plugins: {
          legend: {
            position: 'bottom',
          },
          title: {
            display: true,
            text: 'ATM Status Trend (Last 6 Months)',
            font: {
              size: 16,
            },
          },
        },
        scales: {
          y: {
            beginAtZero: true,
            title: {
              display: true,
              text: 'Number of ATMs',
            },
            // Set max value to total ATMs
            max: totalAtms,
            ticks: {
              stepSize: Math.max(1, Math.floor(totalAtms / 5)),
            },
          },
          x: {
            title: {
              display: true,
              text: 'Month',
            },
          },
        },
      },
    });
  }

  getStatusClass(status: string): string {
    switch (status.toLowerCase()) {
      case 'online':
        return 'bg-green-100 text-green-800';
      case 'offline':
        return 'bg-red-100 text-red-800';
      case 'warning':
        return 'bg-yellow-100 text-yellow-800';
      default:
        return 'bg-gray-100 text-gray-800';
    }
  }
}
