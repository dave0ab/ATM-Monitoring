package com.zemenbank.atmMonitoring.config;

import com.zemenbank.atmMonitoring.repositories.atmMonitor.UserRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.ldap.authentication.ad.ActiveDirectoryLdapAuthenticationProvider;

import lombok.RequiredArgsConstructor;
import org.springframework.web.client.RestTemplate;

@Configuration
@RequiredArgsConstructor
public class ApplicationConfig {
  
  private final UserRepository repository;


  
  @Bean
  public UserDetailsService userDetailsService() {
    return username -> repository.findByDomainId(username)
        .orElseThrow(() -> new UsernameNotFoundException("User not found"));
  }

  @Bean
  ActiveDirectoryLdapAuthenticationProvider authenticationProvider() {
    var ldabAuth =  new ActiveDirectoryLdapAuthenticationProvider("zemenbank.local", "ldap://mbdcp02.zemenbank.local:389", "");
    ldabAuth.setSearchFilter("(&(objectClass=user)(sAMAccountName={1}))");
    return ldabAuth;
  }

  @Bean
  public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
    return config.getAuthenticationManager();
  }

  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }

	// @Bean
	// public WebMvcConfigurer corsConfigurer() {
	// 	return new WebMvcConfigurer() {
	// 		@Override
	// 		public void addCorsMappings(CorsRegistry registry) {
	// 			registry.addMapping("/api/v1/**").allowedOrigins("http://localhost:4200");
	// 		}
	// 	};
	// }


  @Bean
  public RestTemplate restTemplate() {
    return new RestTemplate();
  }

}
