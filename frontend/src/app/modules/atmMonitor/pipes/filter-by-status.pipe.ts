import { Pipe, PipeTransform } from '@angular/core';
import { AtmCurrentStatus } from '../interfaces/atm.interface';

@Pipe({
  name: 'filterByStatus',
  standalone: true,
})
export class FilterByStatusPipe implements PipeTransform {
  transform(atms: AtmCurrentStatus[], status: string): AtmCurrentStatus[] {
    if (!atms) return [];
    return atms.filter((atm) => atm.status === status);
  }
}

@Pipe({
  name: 'arrayLength',
  standalone: true,
})
export class ArrayLengthPipe implements PipeTransform {
  transform(array: any[]): number {
    return array?.length || 0;
  }
}
