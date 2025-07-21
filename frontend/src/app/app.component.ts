import { Component } from '@angular/core';
import {
  NavigationEnd,
  NavigationError,
  Router,
  RouterModule,
} from '@angular/router';
@Component({
  standalone: true,
  imports: [RouterModule],
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.scss'],
})
export class AppComponent {
  title = 'ATM Monitor';
}
