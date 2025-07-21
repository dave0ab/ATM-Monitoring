import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ServerGatewayDashboardComponent } from './server-gateway-monitor.component';

describe('GatewayMonitorComponent', () => {
  let component: ServerGatewayDashboardComponent;
  let fixture: ComponentFixture<ServerGatewayDashboardComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ServerGatewayDashboardComponent],
    }).compileComponents();

    fixture = TestBed.createComponent(ServerGatewayDashboardComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
