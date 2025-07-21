package com.zemenbank.atmMonitoring.repositories.atmMonitor;


import com.zemenbank.atmMonitoring.enums.Category;
import com.zemenbank.atmMonitoring.enums.Roles;
import com.zemenbank.atmMonitoring.model.atmMonitor.Tips;
import com.zemenbank.atmMonitoring.model.atmMonitor.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TipsRepository extends JpaRepository<Tips,Long>{

    List<Tips> findByCategory(Category category);

    List<Tips> findByRolesIn(List<Roles> roles);
}
