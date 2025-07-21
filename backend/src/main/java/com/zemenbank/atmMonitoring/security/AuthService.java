package com.zemenbank.atmMonitoring.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zemenbank.atmMonitoring.dto.request.AuthenticationRequest;
import com.zemenbank.atmMonitoring.dto.request.RegisterRequest;
import com.zemenbank.atmMonitoring.dto.request.UserUpdateRequest;
import com.zemenbank.atmMonitoring.dto.response.AuthenticationResponse;
import com.zemenbank.atmMonitoring.dto.response.RegisterResponse;
import com.zemenbank.atmMonitoring.dto.response.UserResponse;
import com.zemenbank.atmMonitoring.enums.Roles;
import com.zemenbank.atmMonitoring.enums.TokenType;
import com.zemenbank.atmMonitoring.enums.UserStatus;
import com.zemenbank.atmMonitoring.exception.ResourceNotFoundException;
import com.zemenbank.atmMonitoring.exception.ValidationException;
import com.zemenbank.atmMonitoring.model.atmMonitor.Token;
import com.zemenbank.atmMonitoring.model.atmMonitor.User;
import com.zemenbank.atmMonitoring.repositories.atmMonitor.TokenRepository;
import com.zemenbank.atmMonitoring.repositories.atmMonitor.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.security.core.GrantedAuthority;
import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AuthService {

private final UserRepository repository;
  private final TokenRepository tokenRepository;
  private final JwtService jwtService;
  private final AuthenticationManager authenticationManager;

    public RegisterResponse register(RegisterRequest request) {
        try {
            // Additional business validation
            validateRegisterRequest(request);
            
            // Validate the role from the request
            Roles role;
            try {
                role = Roles.valueOf(request.getRole().toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new ValidationException("Invalid role specified: " + request.getRole());
            }

            // Check if the domain ID already exists
            if (repository.findByDomainId(request.getDomainId()).isPresent()) {
                throw new ValidationException("Domain ID already exists");
            }

            // Validate domain ID format (additional business logic)
            if (!isValidDomainIdFormat(request.getDomainId())) {
                throw new ValidationException("Invalid domain ID format. Use format: firstname.lastname (no spaces allowed)");
            }

            // Create the user with the provided domain ID and role
            User user = User.builder()
                    .domainId(request.getDomainId().toLowerCase()) // Normalize to lowercase
                    .role(role)
                    .notificationLevel(request.getNotificationLevel().toUpperCase())
                    .phoneNumber(normalizePhoneNumber(request.getPhoneNumber()))
                    .emailSubscribe(request.isEmailSubscribe())
                    .status(UserStatus.ACTIVE.name())
                    .createdAt(LocalDateTime.now())
                    .build();

            // Save user
            User savedUser = repository.save(user);

            return RegisterResponse.builder()
                    .message("User Registered Successfully")
                    .domainId(savedUser.getDomainId())
                    .role(savedUser.getRole().name())
                    .error(false)
                    .build();
                    
        } catch (ValidationException e) {
            // Re-throw validation exceptions so they can be caught by our global handler
            throw e;
        } catch (Exception e) {
            // Convert any other exceptions to validation exceptions with user-friendly messages
            if (e.getMessage() != null && e.getMessage().contains("Validation failed")) {
                throw new ValidationException("Please check your input and fix any validation errors");
            }
            throw new ValidationException("An error occurred while registering the user. Please try again.");
        }
    }


    public AuthenticationResponse authenticate(AuthenticationRequest request) {

        var user = repository.findByDomainId(request.getDomain() )
				.orElseThrow(()->new ResourceNotFoundException("User with this domain doesnt't exist"));

        if (!user.getStatus().equals("ACTIVE")) {
            throw new IllegalStateException("User is not active");
        }


        try{
    authenticationManager.authenticate(
        new UsernamePasswordAuthenticationToken(
            request.getDomain(),
            request.getUserPass()
        )
    );

    var jwtToken = jwtService.generateToken(user);
    var refreshToken = jwtService.generateRefreshToken(user);
    revokeAllUserTokens(user);
    saveUserToken(user, jwtToken);
    user.setLastSignedOn(Instant.now());
    repository.save(user);
    return AuthenticationResponse.builder()
            .accessToken(jwtToken)
            .refreshToken(refreshToken)
            .user(request.getDomain())
            .build(); 
    }
    catch (Exception e){
        throw e;
    }

  }

  private void saveUserToken(User user, String jwtToken) {
    var token = Token.builder()
        .user(user)
        .token(jwtToken)
        .tokenType(TokenType.BEARER)
        .expired(false)
        .revoked(false)
        .build();
    tokenRepository.save(token);
  }

  private void revokeAllUserTokens(User user) {
    var validUserTokens = tokenRepository.findAllValidTokenByUser(user.getUser_id());
    if (validUserTokens.isEmpty())
      return;
    validUserTokens.forEach(token -> {
      token.setExpired(true);
      token.setRevoked(true);
    });
    tokenRepository.deleteAll(validUserTokens);
  }

  public void refreshToken(
          HttpServletRequest request,
          HttpServletResponse response
  ) throws IOException {
    final String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
    final String refreshToken;
    final String userDomain;
    if (authHeader == null ||!authHeader.startsWith("Bearer ")) {
      return;
    }
    refreshToken = authHeader.substring(7);
    userDomain = jwtService.extractUsername(refreshToken);
    if (userDomain != null) {
      var user = this.repository.findByDomainId(userDomain)
              .orElseThrow();
      if (jwtService.isTokenValid(refreshToken, user)) {
        var accessToken = jwtService.generateToken(user);
        revokeAllUserTokens(user);
        saveUserToken(user, accessToken);
        var authResponse = AuthenticationResponse.builder()
                .user(user.getDomainId())
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
        new ObjectMapper().writeValue(response.getOutputStream(), authResponse);
      }
    }
  }

  public String getLoggedInUsername() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		authentication = SecurityContextHolder.getContext().getAuthentication();
		if (authentication != null && authentication.isAuthenticated()) {
			User user = (User) authentication.getPrincipal();

			return user.getDomainId();
		}

		return null; // or throw an exception if you expect a logged-in user
	}

    public String getLoggedInUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {

            Optional<User> user = repository.findByDomainId(authentication.getName());
            User u = user.get();
            return u.getUser_id().toString(); // Assuming username is used as a unique identifier
        }
        return null; // or throw an exception
    }



    public String getUserRoles() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            Optional<User> user = repository.findByDomainId(authentication.getName());
            User u = user.get();
            return u.getRole().name().toString();  // Returns "ADMIN" as a String
        }
        return null;  // or throw an exception if not authenticated
    }



    public Page<UserResponse> fetchAllUsers(String search, int pageNumber, int pageSize) {
        // Create Pageable with descending sort (example: sorting by "id")
        Pageable pageable = PageRequest.of(pageNumber, pageSize, Sort.by(Sort.Direction.DESC, "createdAt"));

        Page<User> usersPage;

        // Search logic
        if (search != null && !search.isBlank()) {
            usersPage = repository.findByDomainIdContainingIgnoreCase(search, pageable);
        } else {
            usersPage = repository.findAll(pageable);
        }

        // Convert User entity Page to UserResponse Page
        return usersPage.map(this::mapToUserResponse);
    }

    // Update user details
    public UserResponse updateUser(Long userId, UserUpdateRequest request) {
        try {
            User user = repository.findById(userId)
                    .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));

            // Additional business validation for updates
            validateUserUpdateRequest(request, user);

            if (request.getDomainId() != null && !request.getDomainId().trim().isEmpty()) {
                // Check if new domain ID already exists for another user
                Optional<User> existingUser = repository.findByDomainId(request.getDomainId());
                if (existingUser.isPresent() && !existingUser.get().getUser_id().equals(userId)) {
                    throw new ValidationException("Domain ID already exists for another user");
                }
                
                if (!isValidDomainIdFormat(request.getDomainId())) {
                    throw new ValidationException("Invalid domain ID format. Use format: firstname.lastname (no spaces allowed)");
                }
                
                user.setDomainId(request.getDomainId().toLowerCase());
            }
            
            if (request.getRole() != null && !request.getRole().trim().isEmpty()) {
                try {
                    user.setRole(Roles.valueOf(request.getRole().toUpperCase()));
                } catch (IllegalArgumentException e) {
                    throw new ValidationException("Invalid role specified: " + request.getRole());
                }
            }
            
            if (request.getPhoneNumber() != null && !request.getPhoneNumber().trim().isEmpty()) {
                user.setPhoneNumber(normalizePhoneNumber(request.getPhoneNumber()));
            }
            
            if (request.getStatus() != null && !request.getStatus().trim().isEmpty()) {
                user.setStatus(request.getStatus().toUpperCase());
            }
            
            if (request.getNotificationLevel() != null && !request.getNotificationLevel().trim().isEmpty()) {
                user.setNotificationLevel(request.getNotificationLevel().toUpperCase());
            }

            user.setEmailSubscribe(request.isEmailSubscribe());

            User updatedUser = repository.save(user);
            return mapToUserResponse(updatedUser);
            
        } catch (ValidationException e) {
            // Re-throw validation exceptions so they can be caught by our global handler
            throw e;
        } catch (Exception e) {
            // Convert any other exceptions to validation exceptions with user-friendly messages
            if (e.getMessage() != null && e.getMessage().contains("Validation failed")) {
                throw new ValidationException("Please check your input and fix any validation errors");
            }
            throw new ValidationException("An error occurred while updating the user. Please try again.");
        }
    }

    // Deactivate or activate user
    public UserResponse toggleUserStatus(Long userId, boolean activate) {
        User user = repository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));

        user.setStatus(String.valueOf(activate ? UserStatus.ACTIVE : UserStatus.DEACTIVATED));
        User updatedUser = repository.save(user);
        return mapToUserResponse(updatedUser);
    }

    // Delete user
    public void deleteUser(Long userId) {
        User user = repository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));

        repository.delete(user);
    }



    public UserResponse getUserById(Long userId) {
        User user = repository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));


        return UserResponse.builder()
                .id(user.getUser_id())
                .domainId(user.getDomainId())
                .emailSubscribe(user.getEmailSubscribe())
                .role(user.getRole() != null ? user.getRole().name() : null) // Handle null safely
                .status(user.getStatus())
                .notificationLevel(user.getNotificationLevel())
                .createdAt(user.getCreatedAt() != null ? user.getCreatedAt().toString() : null) // Handle null for LocalDateTime
                .lastSignOn(user.getLastSignedOn() != null ? user.getLastSignedOn().toString() : null) // Handle null for Instant
                .build();

    }


    private UserResponse mapToUserResponse(User user) {
        return UserResponse.builder()
                .id(user.getUser_id())
                .domainId(user.getDomainId())
                .phoneNumber(user.getPhoneNumber())
                .role(user.getRole().name())
                .notificationLevel(user.getNotificationLevel())
                .status(user.getStatus())
                .emailSubscribe(user.getEmailSubscribe())
                .build();
    }

    // Validation helper methods
    
    private void validateRegisterRequest(RegisterRequest request) {
        if (request == null) {
            throw new ValidationException("Registration request cannot be null");
        }
        
        if (request.getDomainId() == null || request.getDomainId().trim().isEmpty()) {
            throw new ValidationException("Domain ID cannot be null or empty");
        }
        
        if (request.getRole() == null || request.getRole().trim().isEmpty()) {
            throw new ValidationException("Role cannot be null or empty");
        }
        
        if (request.getPhoneNumber() == null || request.getPhoneNumber().trim().isEmpty()) {
            throw new ValidationException("Phone number cannot be null or empty");
        }
        
        // Additional phone number validation
        validateEthiopianPhoneNumber(request.getPhoneNumber());
        
        if (request.getNotificationLevel() == null || request.getNotificationLevel().trim().isEmpty()) {
            throw new ValidationException("Notification level cannot be null or empty");
        }
    }
    
    private void validateUserUpdateRequest(UserUpdateRequest request, User existingUser) {
        if (request == null) {
            throw new ValidationException("Update request cannot be null");
        }
        
        if (existingUser == null) {
            throw new ValidationException("Existing user cannot be null");
        }
        
        // Validate phone number if provided
        if (request.getPhoneNumber() != null && !request.getPhoneNumber().trim().isEmpty()) {
            validateEthiopianPhoneNumber(request.getPhoneNumber());
        }
        
        // Additional business rules can be added here
        // For example, prevent certain role changes based on current user permissions
    }
    
    private boolean isValidDomainIdFormat(String domainId) {
        if (domainId == null || domainId.trim().isEmpty()) {
            return false;
        }
        
        // Explicitly check for spaces first
        if (domainId.contains(" ")) {
            return false;
        }
        
        // Check if domain ID follows the pattern: firstname.lastname
        // Allow letters, numbers, dots, underscores, and hyphens (NO SPACES)
        return domainId.matches("^[a-zA-Z0-9._-]+$") && 
               domainId.contains(".") && 
               domainId.length() >= 3 && 
               domainId.length() <= 50 &&
               !domainId.startsWith(".") && 
               !domainId.endsWith(".") &&
               !domainId.contains("..");
    }
    
    private String normalizePhoneNumber(String phoneNumber) {
        if (phoneNumber == null || phoneNumber.trim().isEmpty()) {
            return phoneNumber;
        }
        
        // Remove all spaces and non-digit characters except the leading '+'
        String cleaned = phoneNumber.replaceAll("[^0-9+]", "");
        
        // Handle Ethiopian phone number formats
        if (cleaned.startsWith("+251") && cleaned.length() == 13) {
            // Already in international format: +251XXXXXXXXX
            char firstDigit = cleaned.charAt(4);
            if (firstDigit == '7' || firstDigit == '9') {
                return cleaned;
            }
        } else if (cleaned.startsWith("0") && cleaned.length() == 10) {
            // Local format: 0XXXXXXXXX -> convert to +251XXXXXXXXX
            char firstDigit = cleaned.charAt(1);
            if (firstDigit == '7' || firstDigit == '9') {
                return "+251" + cleaned.substring(1);
            }
        } else if (cleaned.startsWith("251") && cleaned.length() == 12) {
            // International without +: 251XXXXXXXXX -> +251XXXXXXXXX
            char firstDigit = cleaned.charAt(3);
            if (firstDigit == '7' || firstDigit == '9') {
                return "+" + cleaned;
            }
        }
        
        // Return as-is if it doesn't match expected patterns
        // (validation will catch invalid formats)
        return cleaned;
    }
    
    private void validateEthiopianPhoneNumber(String phoneNumber) {
        if (phoneNumber == null || phoneNumber.trim().isEmpty()) {
            return; // Will be caught by @NotBlank
        }
        
        String cleaned = phoneNumber.replaceAll("[^0-9+]", "");
        
        // Check for valid Ethiopian phone number patterns
        boolean isValid = false;
        
        if (cleaned.startsWith("+251") && cleaned.length() == 13) {
            // International format: +251XXXXXXXXX
            char firstDigit = cleaned.charAt(4);
            isValid = (firstDigit == '7' || firstDigit == '9');
        } else if (cleaned.startsWith("0") && cleaned.length() == 10) {
            // Local format: 0XXXXXXXXX
            char firstDigit = cleaned.charAt(1);
            isValid = (firstDigit == '7' || firstDigit == '9');
        } else if (cleaned.startsWith("251") && cleaned.length() == 12) {
            // International without +: 251XXXXXXXXX
            char firstDigit = cleaned.charAt(3);
            isValid = (firstDigit == '7' || firstDigit == '9');
        }
        
        if (!isValid) {
            throw new ValidationException(
                "Invalid phone number format. Use Ethiopian format: +251920742742, +251720743743, 0920742742, or 0720743743");
        }
        
        // Additional validation: check if it's too short like "251"
        if (cleaned.length() < 10) {
            throw new ValidationException(
                "Phone number is too short. Ethiopian numbers must be 10 digits (local) or 13 digits (international). You entered: " + cleaned);
        }
    }

}
