import { Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Activity, ArrowUpDown, Server, Wifi } from 'lucide-angular';
import { Router } from '@angular/router';
import { Store } from '@ngrx/store';
import { Observable, Subject, takeUntil, map } from 'rxjs';
import {
  FetchGatewayMonitortState,
  ServerGateway,
} from '../../interfaces/atm.interface';
import { fetchGatewayMonitor, fetchAtm } from '../../store/atm/atm.actions';
import {
  selectGatewayMonitorData,
  selectGatewayMonitorLoading,
  selectGatewayMonitorError,
} from '../../store/atm/atm.selectors';
import { WebSocketService } from '../../services/web-socket.service';

@Component({
  selector: 'app-server-gateway-monitor',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './server-gateway-monitor.component.html',
  styleUrls: ['./server-gateway-monitor.component.scss'],
})
export class ServerGatewayDashboardComponent implements OnInit, OnDestroy {
  gateways: ServerGateway[] = [];
  selectedGateway: ServerGateway | null = null;

  fetch$: Observable<ServerGateway[]>;
  loading$: Observable<boolean>;
  error$: Observable<string | null>;
  expandedGateways: Set<string> = new Set();
  private destroy$ = new Subject<void>();
  updateInterval: any;
  private intervalId: any;

  constructor(
    private store: Store,
    private router: Router,
    private WebSocketService: WebSocketService
  ) {
    this.fetch$ = this.store
      .select(selectGatewayMonitorData)
      .pipe(map((state) => state || []));
    this.loading$ = this.store.select(selectGatewayMonitorLoading);
    this.error$ = this.store.select(selectGatewayMonitorError);
  }

  ngOnInit() {
    this.initializeData();

    this.WebSocketService.getGatewayUpdates()
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (gatewayUpdate) => {
          this.updateTableWithNewData(gatewayUpdate);
        },
      });

    this.intervalId = setInterval(() => {
      this.initializeData();
    }, 60000);
  }

  updateTableWithNewData(gatewayUpdate: ServerGateway) {
    if (!gatewayUpdate) return;

    this.gateways = this.gateways || [];

    const index = this.gateways.findIndex(
      (gateway) => gateway.network === gatewayUpdate.network
    );

    if (index !== -1) {
      this.gateways = [
        ...this.gateways.slice(0, index),
        { ...this.gateways[index], ...gatewayUpdate },
        ...this.gateways.slice(index + 1),
      ];
    } else {
      this.gateways = [...this.gateways, gatewayUpdate];
    }
  }
  ngOnDestroy() {
    this.destroy$.next();
    this.destroy$.complete();

    if (this.intervalId) {
      clearInterval(this.intervalId);
    }
  }

  private initializeData() {
    this.store.dispatch(fetchGatewayMonitor());
    this.fetch$
      .pipe(
        takeUntil(this.destroy$),
        map((response) => {
          if (response) {
            this.gateways = Array.isArray(response) ? response : [response];
          }
        })
      )
      .subscribe();
  }

  toggleDetails(gateway: ServerGateway) {
    this.expandedGateways.has(gateway.systemId)
      ? this.expandedGateways.delete(gateway.systemId)
      : this.expandedGateways.add(gateway.systemId);
  }

  isExpanded(gateway: ServerGateway): boolean {
    return this.expandedGateways.has(gateway.systemId);
  }

  openModal(gateway: ServerGateway) {
    this.selectedGateway = gateway;
  }

  closeModal() {
    this.selectedGateway = null;
  }
}
