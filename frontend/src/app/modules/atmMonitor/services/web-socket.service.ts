import { Injectable } from '@angular/core';
import { Observable, Subject } from 'rxjs';
import { Client, Message } from '@stomp/stompjs';
import { environment } from 'src/environments/environment';
import { Atm, ServerGateway } from '../interfaces/atm.interface';
import { AuthService } from '../../auth/services/auth.service';

@Injectable({
  providedIn: 'root',
})
export class WebSocketService {
  private client: Client | null = null;
  private atmUpdatesSubject = new Subject<Atm>();
  private gatewayUpdatesSubject = new Subject<ServerGateway>();

  constructor(private authService: AuthService) {
    // this.authService.getCurrentuser().subscribe((user) => {
    //   if (user) {
    //     this.initializeWebSocket(user.access_token);
    //   } else {
    //     this.disconnect();
    //   }
    // });
  }

  private initializeWebSocket(accessToken: string) {
    if (this.client && this.client.connected) {
      return;
    }

    this.client = new Client({
      brokerURL: environment.wsUrl,
      connectHeaders: {
        Authorization: `Bearer ${accessToken}`,
      },
      reconnectDelay: 5000,
      heartbeatIncoming: 4000,
      heartbeatOutgoing: 4000,

      onConnect: () => {
        this.client?.subscribe('/topic/atms/status', (message: Message) => {
          const atmUpdate: Atm = JSON.parse(message.body);
          this.atmUpdatesSubject.next(atmUpdate);
        });

        this.client?.subscribe('/topic/gateway/updates', (message: Message) => {
          const gatewayUpdate: ServerGateway = JSON.parse(message.body);
          this.gatewayUpdatesSubject.next(gatewayUpdate);
        });
      },
      onStompError: (frame) => {},
    });

    this.client.activate();
  }

  public getAtmUpdates(): Observable<Atm> {
    return this.atmUpdatesSubject.asObservable();
  }

  public getGatewayUpdates(): Observable<ServerGateway> {
    return this.gatewayUpdatesSubject.asObservable();
  }

  public disconnect() {
    if (this.client && this.client.connected) {
      this.client.deactivate();
    }
    this.client = null;
  }
}
