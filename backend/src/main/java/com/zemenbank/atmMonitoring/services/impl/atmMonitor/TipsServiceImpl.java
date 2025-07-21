package com.zemenbank.atmMonitoring.services.impl.atmMonitor;

import com.zemenbank.atmMonitoring.dto.request.TipsDTO;
import com.zemenbank.atmMonitoring.enums.Category;
import com.zemenbank.atmMonitoring.enums.Roles;
import com.zemenbank.atmMonitoring.model.atmMonitor.Tips;
import com.zemenbank.atmMonitoring.repositories.atmMonitor.*;
import com.zemenbank.atmMonitoring.repositories.bankworld.ATMCashRepository;
import com.zemenbank.atmMonitoring.repositories.bankworld.ATMSRepository;
import com.zemenbank.atmMonitoring.repositories.bankworld.ATM_NetworkRepository;
import com.zemenbank.atmMonitoring.security.AuthService;
import com.zemenbank.atmMonitoring.services.ATMCacheService;
import com.zemenbank.atmMonitoring.services.TipsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class TipsServiceImpl implements TipsService {


    private TipsRepository tipsRepository;

    private AuthService authService;


    public TipsServiceImpl(
            TipsRepository tipsRepository,
            AuthService authService

            // Removed ATMAccesses here
    ) {
        this.tipsRepository = tipsRepository;
        this.authService = authService;

    }
    @Override
    public Tips createTips(Tips tips) {
        if (tips.getRoles() == null || tips.getRoles().isEmpty()) {
            throw new IllegalArgumentException("At least one role must be associated with the tip");
        }
        tips.setCreatedDate(LocalDateTime.now()); // Automatically set created date
        return tipsRepository.save(tips);
    }

    @Override
    public Tips updateTips(Long id, Tips tips) {
        Optional<Tips> existingTips = tipsRepository.findById(id);
        if (existingTips.isPresent()) {
            Tips updatedTips = existingTips.get();

            // Update fields
            updatedTips.setCategory(tips.getCategory());
            updatedTips.setTitle(tips.getTitle());
            updatedTips.setDetails(tips.getDetails());
            updatedTips.setCreatedDate(tips.getCreatedDate());

            // Update roles
            if (tips.getRoles() != null && !tips.getRoles().isEmpty()) {
                updatedTips.setRoles(tips.getRoles());
            } else {
                throw new IllegalArgumentException("Roles cannot be empty during update");
            }

            return tipsRepository.save(updatedTips);
        }
        throw new RuntimeException("Tips with ID " + id + " not found");
    }


    @Override
    public void deleteTips(Long id) {
        tipsRepository.deleteById(id);
    }

    @Override
    public Tips getTipsById(Long id) {
        Tips tip = tipsRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Tips with ID " + id + " not found"));

        String userRoles = authService.getUserRoles(); // Get roles of the logged-in user
        boolean hasAccess = tip.getRoles().stream()
                .anyMatch(role -> userRoles.contains(role.name())); // Check if user roles match

        if (!hasAccess) {
            throw new RuntimeException("You do not have permission to view this tip");
        }

        return tip;
    }


    @Override
    public List<Tips> getAllTips() {
        String userRole = authService.getUserRoles(); // Get the single role of the logged-in user

        // Admins can see all tips
        if ("ADMIN".equals(userRole)) {
            return tipsRepository.findAll();
        }

        // Convert the single role string to a Roles enum
        Roles currentUserRole = Roles.valueOf(userRole);

        // Filter tips based on the user's single role
        return tipsRepository.findAll().stream()
                .filter(tip -> tip.getRoles().contains(currentUserRole))
                .collect(Collectors.toList());
    }


    @Override
    public List<Tips> getTipsByCategory(Category category) {
        return tipsRepository.findByCategory(category);
    }

    // Static Nested Class for Mapping
    public static class TipsMapper {

        public static Tips toEntity(TipsDTO dto) {
            Tips tips = new Tips();
            tips.setId(dto.getId());
            tips.setCategory(Category.valueOf(dto.getCategory())); // Convert string to enum
            tips.setTitle(dto.getTitle());
            tips.setDetails(dto.getDetails());
            return tips;
        }

        public static TipsDTO toDTO(Tips tips) {
            TipsDTO dto = new TipsDTO();
            dto.setId(tips.getId());
            dto.setCategory(tips.getCategory().name()); // Convert enum to string
            dto.setTitle(tips.getTitle());
            dto.setDetails(tips.getDetails());
            return dto;
        }

        public static List<TipsDTO> toDTOList(List<Tips> tipsList) {
            return tipsList.stream().map(TipsMapper::toDTO).collect(Collectors.toList());
        }

        public static List<Tips> toEntityList(List<TipsDTO> tipsDTOList) {
            return tipsDTOList.stream().map(TipsMapper::toEntity).collect(Collectors.toList());
        }
    }
}
