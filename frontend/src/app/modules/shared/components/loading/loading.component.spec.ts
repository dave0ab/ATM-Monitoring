import { ComponentFixture, TestBed } from '@angular/core/testing';
import { AlertSuccessComponent } from '../alerts/alert-success/alert-success.component';
import { LoadingComponent } from './loading.component';

describe('AlertSuccessComponent', () => {
  let component: LoadingComponent;
  let fixture: ComponentFixture<LoadingComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [LoadingComponent],
    }).compileComponents();

    fixture = TestBed.createComponent(LoadingComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
