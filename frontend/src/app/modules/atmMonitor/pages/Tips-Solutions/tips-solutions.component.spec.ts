import { ComponentFixture, TestBed } from '@angular/core/testing';
import { TipsSolutionsComponent } from './tips-solutions.component';

describe('TipsSolutionsComponent', () => {
  let component: TipsSolutionsComponent;
  let fixture: ComponentFixture<TipsSolutionsComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [TipsSolutionsComponent],
    }).compileComponents();

    fixture = TestBed.createComponent(TipsSolutionsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
