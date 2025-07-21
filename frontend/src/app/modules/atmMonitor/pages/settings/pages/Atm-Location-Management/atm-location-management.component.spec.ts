import { ComponentFixture, TestBed } from '@angular/core/testing';
import { AtmLocationManagementComponent } from './atm-location-management.component';

describe('AtmLocationManagementComponent', () => {
  let component: AtmLocationManagementComponent;
  let fixture: ComponentFixture<AtmLocationManagementComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [AtmLocationManagementComponent],
    }).compileComponents();

    fixture = TestBed.createComponent(AtmLocationManagementComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
