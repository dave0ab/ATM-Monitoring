import { TestBed } from '@angular/core/testing';

import { NavmenusService } from './navmenus.service';

describe('NavmenusService', () => {
  let service: NavmenusService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(NavmenusService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
