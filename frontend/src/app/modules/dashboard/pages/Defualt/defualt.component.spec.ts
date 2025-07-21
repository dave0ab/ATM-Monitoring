import { ComponentFixture, TestBed } from '@angular/core/testing';
import { DefualtComponent } from './defualt.component';


describe('DefualtComponent', () => {
  let component: DefualtComponent;
  let fixture: ComponentFixture<DefualtComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [DefualtComponent],
    }).compileComponents();

    fixture = TestBed.createComponent(DefualtComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
