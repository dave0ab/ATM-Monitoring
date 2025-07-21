package com.zemenbank.atmMonitoring.controller.v1.atmMonitor;

import com.zemenbank.atmMonitoring.controller.BaseController;
import com.zemenbank.atmMonitoring.dto.request.AuthenticationRequest;
import com.zemenbank.atmMonitoring.dto.request.RegisterRequest;
import com.zemenbank.atmMonitoring.dto.request.UserUpdateRequest;
import com.zemenbank.atmMonitoring.dto.response.AuthenticationResponse;
import com.zemenbank.atmMonitoring.dto.response.RegisterResponse;
import com.zemenbank.atmMonitoring.dto.response.UserResponse;
import com.zemenbank.atmMonitoring.security.AuthService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Tag(
        name = "Authentication REST APIs for Legal Department user",
        description = "REST APIs - Register new Legal user, Authenticate new user, and manage users"
)
@RestController
@RequestMapping("/v1/auth")
@RequiredArgsConstructor
public class AuthenticationController extends BaseController {

  private final AuthService service;



  @PostMapping("/register")
  public ResponseEntity<RegisterResponse> register(
          @Valid @RequestBody RegisterRequest request
  ) {
    return ResponseEntity.ok(service.register(request));
  }

  @PostMapping("/authenticate")
  public ResponseEntity<AuthenticationResponse> authenticate(
          @RequestBody AuthenticationRequest request
  ) {
    return ResponseEntity.ok(service.authenticate(request));
  }

  @PostMapping("/refresh-token")
  public void refreshToken(
          HttpServletRequest request,
          HttpServletResponse response
  ) throws IOException {
    service.refreshToken(request, response);
  }

  // Fetch all users with optional search

  @GetMapping("/users")
  public ResponseEntity<Page<UserResponse>> fetchAllUsers(
          @RequestParam(value = "pageNumber", defaultValue = "0", required = false) int pageNumber,
          @RequestParam(value = "pageSize", defaultValue = "10", required = false) int pageSize,
          @RequestParam(value = "search", required = false, defaultValue = "") String search
  ) {
    return ResponseEntity.ok(service.fetchAllUsers(search, pageNumber, pageSize));
  }

  @PostMapping("/getLoggedInUserRole")
  public ResponseEntity<Map<String, String>> getUserRole() {
    String role = service.getUserRoles();
    Map<String, String> response = new HashMap<>();
    response.put("role", role);
    return ResponseEntity.ok(response);  // Respond with status 200 OK and the role in JSON
  }


  // Update user details
  @PutMapping("/users/{userId}")
  public ResponseEntity<UserResponse> updateUser(
          @PathVariable Long userId,
          @Valid @RequestBody UserUpdateRequest request
  ) {
    return ResponseEntity.ok(service.updateUser(userId, request));
  }

  // Toggle user status (activate/deactivate)
  @PatchMapping("/users/{userId}/status")
  public ResponseEntity<UserResponse> toggleUserStatus(
          @PathVariable Long userId,
          @RequestParam boolean activate
  ) {
    return ResponseEntity.ok(service.toggleUserStatus(userId, activate));
  }


  @GetMapping("/users/{userId}")
  public ResponseEntity<UserResponse> getUser(
          @PathVariable Long userId

  ) {
    return ResponseEntity.ok(service.getUserById(userId));
  }

  // Delete user
  @DeleteMapping("/users/{userId}")
  public ResponseEntity<Void> deleteUser(
          @PathVariable Long userId
  ) {
    service.deleteUser(userId);
    return ResponseEntity.noContent().build();
  }
}
