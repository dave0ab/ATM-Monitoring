package com.zemenbank.atmMonitoring.model.atmMonitor;

import com.zemenbank.atmMonitoring.enums.Category;
import com.zemenbank.atmMonitoring.enums.Roles;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Set;

@Entity
@Data
@Table(name = "TIPS")
public class Tips {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "ID")
  private Long id;

  @Enumerated(EnumType.STRING)
  @Column(name = "CATEGORY")
  private Category category;

  @Column(name = "TITLE")
  private String title;

  @Column(name = "DETAILS", length = 10000)
  private String details;

  @Column(name = "CREATED_DATE")
  private LocalDateTime createdDate;

  @ElementCollection(fetch = FetchType.EAGER)
  @Enumerated(EnumType.STRING)
  @CollectionTable(name = "TIPS_ROLES", joinColumns = @JoinColumn(name = "TIPS_ID"))
  @Column(name = "ROLE")
  private Set<Roles> roles; // Allow multiple roles to view the tip
}
