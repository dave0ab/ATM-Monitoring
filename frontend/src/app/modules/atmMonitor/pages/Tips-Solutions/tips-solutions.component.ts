import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import {
  FormBuilder,
  FormGroup,
  Validators,
  ReactiveFormsModule,
  FormsModule,
} from '@angular/forms';
import { Store } from '@ngrx/store';
import { Observable } from 'rxjs';
import { Tip } from '../../interfaces/navmenu.interface';
import {
  selectAllTips,
  selectTipsLoading,
} from '../../store/navmenus/navmenus.selectors';
import * as TipsActions from '../../store/navmenus/navmenus.actions';
import { map } from 'rxjs/operators';
import { FilterPipe } from '../../shared/pipes/filter.pipe';
import { AuthService } from 'src/app/modules/auth/services/auth.service';

@Component({
  selector: 'app-tips-solutions',
  templateUrl: './tips-solutions.component.html',
  styleUrls: ['./tips-solutions.component.scss'],
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, FormsModule, FilterPipe],
})
export class TipsSolutionsComponent implements OnInit {
  searchTerm: string = '';
  tips$: Observable<Tip[]>;
  loading$: Observable<boolean>;
  showAddModal = false;
  tipForm: FormGroup;
  openItems = new Set<number>();
  isAdmin = false;
  editingTip: Tip | null = null;

  roles = [
    { name: 'IT', checked: false },
    { name: 'OMNI CHANNEL', checked: false },
    { name: 'BRANCH', checked: false },
  ];

  constructor(
    private store: Store,
    private fb: FormBuilder,

    private authService: AuthService
  ) {
    this.tips$ = this.store.select(selectAllTips);
    this.loading$ = this.store.select(selectTipsLoading);

    // Initialize the form with default values
    this.tipForm = this.fb.group({
      title: ['', Validators.required],
      details: ['', Validators.required],
      category: ['FAQ', Validators.required],
      roles: [[], Validators.required],
    });

    this.isAdmin = this.authService.isAdmin();
  }

  ngOnInit() {
    // Dispatch action to load tips on component initialization
    this.store.dispatch(TipsActions.loadTips());
  }

  // Open the "Add Tip" modal
  openAddModal() {
    this.showAddModal = true;
  }

  // Close the "Add Tip" modal and reset the form
  closeAddModal() {
    this.showAddModal = false;
    this.editingTip = null;
    this.tipForm.reset({ category: 'FAQ', roles: [] });
    this.roles.forEach((role) => (role.checked = false));
  }

  // Submit the form and dispatch the "Create Tip" action
  submitTip() {
    const selectedRoles = this.roles
      .filter((role) => role.checked)
      .map((role) => role.name);

    this.tipForm.patchValue({ roles: selectedRoles });

    if (this.tipForm.valid) {
      const tipData = {
        title: this.tipForm.value.title,
        details: this.tipForm.value.details,
        category: this.tipForm.value.category,
        roles: selectedRoles,
        createdDate: new Date().toISOString(),
      };

      if (this.editingTip) {
        // Update existing tip
        this.store.dispatch(
          TipsActions.updateTip({
            id: this.editingTip.id,
            tip: tipData,
          })
        );
      } else {
        // Create new tip
        this.store.dispatch(TipsActions.createTip({ tip: tipData }));
      }

      this.closeAddModal();
    }
  }

  // Toggle role selection
  toggleRole(role: { name: string; checked: boolean }) {
    role.checked = !role.checked;
    const selectedRoles = this.roles
      .filter((r) => r.checked)
      .map((r) => r.name);
    this.tipForm.patchValue({ roles: selectedRoles });
  }

  // Filter tips based on category
  getFilteredTips(category: 'FAQ' | 'SOLUTIONS' | 'TIPS') {
    return this.tips$.pipe(
      map((tips) =>
        tips
          .filter((tip) => tip.category === category)
          .map((tip) => ({
            ...tip,
            isOpen: this.openItems.has(tip.id),
          }))
      )
    );
  }

  // Toggle the expanded state of a tip item
  toggleItem(item: Tip) {
    if (this.openItems.has(item.id)) {
      this.openItems.delete(item.id);
    } else {
      this.openItems.add(item.id);
    }
  }

  // Placeholder for the "Chat with Support" functionality
  openChat() {
    console.log('Chat with Support clicked');
  }

  // Add edit method
  editTip(tip: Tip) {
    if (!this.isAdmin) return;
    this.editingTip = tip;

    this.tipForm.patchValue({
      title: tip.title,
      details: tip.details,
      category: tip.category,
      roles: tip.roles,
    });

    this.roles.forEach((role) => {
      role.checked = tip.roles.includes(role.name);
    });

    this.showAddModal = true;
  }
}
