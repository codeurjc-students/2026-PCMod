package es.codeurjcstudents.pcmod.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import es.codeurjcstudents.pcmod.security.jwt.JwtRequestFilter;
import es.codeurjcstudents.pcmod.security.jwt.JwtTokenProvider;
import es.codeurjcstudents.pcmod.security.jwt.UnauthorizedHandlerJwt;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

  @Autowired
  private JwtTokenProvider jwtTokenProvider;

  @Autowired
  public RepositoryUserDetailsService userDetailService;

  @Autowired
  private UnauthorizedHandlerJwt unauthorizedHandlerJwt;

  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }

  @Bean
  public DaoAuthenticationProvider authenticationProvider() {
    DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider(userDetailService);
    authProvider.setPasswordEncoder(passwordEncoder());

    return authProvider;
  }

  @Bean
  public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
    return authConfig.getAuthenticationManager();
  }

  @Bean
  SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

    http.authenticationProvider(authenticationProvider());

    http.securityMatcher("/api/**")
        .exceptionHandling(handling -> handling.authenticationEntryPoint(unauthorizedHandlerJwt));

    http.authorizeHttpRequests(authorize -> authorize.anyRequest().permitAll());

    http.formLogin(formLogin -> formLogin.disable());
    http.csrf(csrf -> csrf.disable());
    http.httpBasic(httpBasic -> httpBasic.disable());
    http.sessionManagement(management -> management.sessionCreationPolicy(SessionCreationPolicy.STATELESS));
    http.addFilterBefore(new JwtRequestFilter(userDetailService, jwtTokenProvider),
        UsernamePasswordAuthenticationFilter.class);

    return http.build();
  }
}