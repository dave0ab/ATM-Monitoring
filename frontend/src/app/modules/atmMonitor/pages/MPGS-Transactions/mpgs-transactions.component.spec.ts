import { ComponentFixture, TestBed } from '@angular/core/testing';
import { MPGSTransactionsComponent } from './mpgs-transactions.component';

describe('MPGSTransactionsComponent', () => {
  let component: MPGSTransactionsComponent;
  let fixture: ComponentFixture<MPGSTransactionsComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [MPGSTransactionsComponent],
    }).compileComponents();

    fixture = TestBed.createComponent(MPGSTransactionsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
